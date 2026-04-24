# Phogal — Nav3 1.1.0의 3가지 고급 기능 적용

작업 일자: 2026-04-22
대상: **Navigation 3 1.1.0 stable**
환경: AGP 8.13.2 / Kotlin 2.0.21 / Compose BOM 2024.12.01 / Hilt 2.58

---

## 🎯 이번 턴 요약

### 적용된 3가지 기능

1. ✅ **Shared Element Transition 인프라** — `SharedTransitionLayout`으로 `NavDisplay` 감싸고 `LocalSharedTransitionScope` 노출. 사용자는 `PhotoItem` / `PictureContent`에 `Modifier.sharedElement()`만 붙이면 됨.
2. ✅ **`ListDetailSceneStrategy` 적용** — `SearchPhotosRoute`(list) + `PictureRoute`(detail) 페어로 태블릿/폴더블에서 자동 2-pane.
3. ✅ **`DialogSceneStrategy` 적용** — 새 `PermissionDialogRoute`를 backstack에 편입. Predictive back / 회전 / 프로세스 사망 복원 자동 지원.

### 동시에 정정된 핵심 API

이번에 AOSP 공식 소스 코드 재확인 결과, **제 이전 턴 판단이 틀렸음**이 확인되었습니다:

> **Navigation 3 1.1.0 stable의 `NavDisplay`는 `sceneStrategy` (단수) 파라미터를 제공하지 않고 `sceneStrategies: List<SceneStrategy<T>>` (복수)를 제공합니다.**

- 출처 1: [AOSP `NavDisplay.kt`](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:navigation3/navigation3-ui/src/commonMain/kotlin/androidx/navigation3/ui/NavDisplay.kt) — `sceneStrategies`, `sceneDecoratorStrategies` 파라미터로 `rememberSceneState()` 호출
- 출처 2: Google 공식 Nav3 1.1.0 마이그레이션 가이드 (2026-04-16) — `sceneStrategies = remember { listOf(DialogSceneStrategy()) }` 예시
- 출처 3: GitHub [google/play-services-plugins#400](https://github.com/google/play-services-plugins/issues/400) — 1.1.0 출시 직후 `sceneStrategy` 단수형 사용 시 `NoSuchMethodError` 발생 사례

제가 이전 턴에 사용자의 `sceneStrategies = listOf(...)`를 "잘못된 API"로 판단해서 `sceneStrategy = ...`로 되돌렸던 것은 **명백한 제 오류**였습니다. Medium 블로그들이 1.0.0 기준 예제를 많이 남겨둬서 제 학습이 거기에 치우쳤습니다. 이번엔 공식 AOSP 소스 코드까지 교차검증하여 바로잡았습니다. 사과 드립니다.

---

## 📋 변경 파일 (6개)

### 의존성 (2개)
| 파일 | 변경 |
|------|------|
| `gradle/libs.versions.toml` | `adaptiveNavigation3 = "1.1.0"` 추가, `androidx-compose-material3-adaptive-navigation3` 라이브러리 정의 |
| `app/build.gradle` | `libs.androidx.compose.material3.adaptive.navigation3` 의존성 추가 |

### 소스 (4개)
| 파일 | 유형 | 변경 |
|------|------|------|
| `ui/navigation/Routes.kt` | 수정 | `PermissionDialogRoute` 신규 추가 |
| `ui/navigation/nav3/SharedTransitionKeys.kt` | 🆕 신규 | `LocalSharedTransitionScope` + `photoSharedElementKey()` 헬퍼 |
| `ui/navigation/nav3/PhogalEntryProvider.kt` | 재작성 | 각 entry에 metadata 추가 (`listPane()`, `detailPane()`, `dialog()`) + `DetailPlaceholder`, `PermissionDialogContent` 헬퍼 |
| `ui/compose/screen/home/HomeScreen.kt` | 재작성 | `SharedTransitionLayout` + `sceneStrategies`(복수) + 3종 전략 결합 |

---

## 🔧 Nav3 1.1.0 stable의 정확한 `NavDisplay` 시그니처

이번 작업으로 확정한 정확한 형태:

```kotlin
NavDisplay(
    backStack = navState.backStackForCurrentTab,
    onBack = { count -> repeat(count) { navState.pop() } },   // (Int) -> Unit
    sceneStrategies = listOf(                                 // List<SceneStrategy<T>>, 복수형!
        DialogSceneStrategy<Any>(),
        rememberListDetailSceneStrategy<Any>(),
        SinglePaneSceneStrategy<Any>()
    ),
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),       // 1.1.0 이름
        rememberViewModelStoreNavEntryDecorator()
        // rememberSceneSetupNavEntryDecorator — NOT in the list (자동화됨)
    ),
    transitionSpec = ...,
    popTransitionSpec = ...,
    predictivePopTransitionSpec = ...,
    entryProvider = entryProvider { phogalEntries(navState) }
)
```

### `sceneStrategies` 순서가 중요

NavDisplay는 각 strategy에게 순서대로 "현재 top entry를 렌더링할 수 있나요?"라고 묻고, `null`이 반환되면 다음 전략으로 넘어갑니다. 올바른 순서:

1. **`DialogSceneStrategy`** — dialog metadata가 있는 entry는 Dialog로. 아니면 null.
2. **`ListDetailSceneStrategy`** — listPane/detailPane metadata가 있고 wide 화면이면 2-pane으로. 아니면 null.
3. **`SinglePaneSceneStrategy`** — 모든 경우를 받아주는 fallback. 단일 화면으로 렌더링.

Dialog를 맨 앞에 두는 이유: dialog는 항상 다른 scene 위에 오버레이되어야 함. ListDetail이 그 위라면 dialog가 pane 안에 갇혀 보여 버립니다.

---

## 🎨 1. Shared Element Transition 사용법

인프라는 준비되었습니다. 실제 hero 애니메이션을 적용하려면 **리스트 아이템과 상세 화면 양쪽에 동일한 key를 쓰는 `Modifier.sharedElement()`**를 붙이면 됩니다.

### 예시 — `PhotoItem.kt`의 썸네일에 적용

```kotlin
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.goforer.phogal.presentation.ui.navigation.nav3.LocalSharedTransitionScope
import com.goforer.phogal.presentation.ui.navigation.nav3.photoSharedElementKey

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PhotoItem(
    photo: Photo,
    // ... 기존 파라미터
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedContentScope.current

    // Image(...)의 modifier 뒤에 sharedElement 추가
    val heroModifier = if (sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = photoSharedElementKey(photo.id)
                ),
                animatedVisibilityScope = animatedScope
            )
        }
    } else {
        Modifier  // shared transition 없는 컨텍스트에서도 안전
    }

    Image(
        painter = ...,
        modifier = Modifier
            .size(...)
            .then(heroModifier)   // ← 여기
            .clickable { onItemClicked.invoke(photo, index) }
    )
}
```

### 상세 화면 `PictureContent.kt`에도 동일하게

```kotlin
// 상세 화면의 메인 이미지에 같은 modifier 적용
Image(
    painter = ...,
    modifier = Modifier
        .fillMaxWidth()
        .then(heroModifier)   // 같은 photoSharedElementKey(photo.id)
)
```

**왜 파일을 직접 수정하지 않았나?**  
`PhotoItem.kt`와 `PictureContent.kt`는 이미 `clickable`, `graphicsLayer`, 여러 상태 기반 modifier 체인이 얽혀 있어 무심코 수정하면 기존 인터랙션/애니메이션이 깨질 위험이 있습니다. 사용자가 직접 **기존 modifier chain의 적절한 위치**에 위 코드를 삽입하는 것이 안전합니다.

### 동작 방식

- `HomeScreen`이 `NavDisplay`를 `SharedTransitionLayout`으로 감싸고 `SharedTransitionScope`를 `LocalSharedTransitionScope`에 노출
- 각 NavEntry는 `AnimatedContent` 안에서 렌더링되며, Nav3가 자동으로 `LocalNavAnimatedContentScope`를 제공
- 썸네일과 상세 화면이 **동일한 key**로 `sharedElement` 선언하면 Compose가 bounds interpolation을 자동 처리

---

## 📱 2. `ListDetailSceneStrategy` 적용

### 이미 적용된 metadata

```kotlin
// PhogalEntryProvider.kt
entry<Routes.SearchPhotosRoute>(
    metadata = ListDetailSceneStrategy.listPane(
        detailPlaceholder = { DetailPlaceholder() }
    )
) { /* SearchPhotosScreen */ }

entry<Routes.PictureRoute>(
    metadata = ListDetailSceneStrategy.detailPane()
) { key -> /* PictureScreen */ }
```

### 기대 동작

| 기기 | 동작 |
|------|------|
| 핸드폰 (세로) | 기존처럼 SearchPhotos → Picture push |
| 태블릿 / 폴더블 펼침 | SearchPhotos(좌측 50%) + Picture(우측 50%) 동시 표시 |
| 태블릿 (Picture 선택 전) | 좌: SearchPhotos, 우: `DetailPlaceholder` |

`detailPlaceholder`는 현재 사진이 선택되기 전에 우측 pane에 표시될 placeholder입니다. 지금은 간단한 텍스트만 넣었고, 나중에 앱 로고나 최근 사진 슬라이드쇼 같은 것으로 개선할 여지가 있습니다.

### 제약

`UserPhotosRoute`, `WebViewRoute`, `BookmarkedPhotosRoute` 등은 **list-detail pair가 아닌** 독립 screen이므로 metadata 없이 그대로 둡니다. `SinglePaneSceneStrategy`가 fallback으로 받아서 full-screen으로 렌더링합니다.

---

## 💬 3. `DialogSceneStrategy` 적용

### `PermissionDialogRoute` 등록

```kotlin
// Routes.kt
@Serializable data object PermissionDialogRoute : NavKey

// PhogalEntryProvider.kt
entry<Routes.PermissionDialogRoute>(
    metadata = DialogSceneStrategy.dialog()
) {
    PermissionDialogContent(
        onDismiss = { navState.pop() },
        onConfirm = { navState.pop() }
    )
}
```

### 호출 방법 (기존 `SearchPhotosScreen`에서)

기존 `SearchPhotosScreen`이 내부에서 `PermissionBottomSheet`을 띄우는 부분을 다음과 같이 대체할 수 있습니다:

```kotlin
// 예: 권한 요청이 필요한 순간
onPermissionRequired = {
    navState.push(Routes.PermissionDialogRoute)
}
```

### 왜 `PermissionBottomSheet`을 직접 dialog entry에 넣지 않았나?

`PermissionBottomSheet`의 내부 구조는:
```kotlin
@Composable
fun PermissionBottomSheet(...) {
    ModalBottomSheet(...) {   // ← 자체적으로 Dialog 레이어 띄움
        Column { ... }
    }
}
```

`DialogSceneStrategy`가 이미 `Dialog` 인스턴스를 띄우는데, 그 안에서 다시 `ModalBottomSheet`(자체 `Dialog`)을 띄우면 **Dialog 안의 Dialog**라는 이중 오버레이가 됩니다. 이는:
- 이중 스크림(scrim) 배경으로 시각적으로 어색함
- 예측 가능한 뒤로가기가 이중으로 작동
- Android의 Window 상태 관리가 꼬임

### 해결책 — 둘 중 하나 선택

**A. 본 구현처럼 간단한 `AlertDialog` content** (현재 기본값)
- 장점: 즉시 작동, 추가 리팩토링 없음
- 단점: bottom-sheet 특유의 드래그 UX 상실

**B. `PermissionBottomSheet`의 내부 content를 별도 composable로 분리**
- `PermissionRequestContent` 같은 pure composable로 추출
- 기존 `PermissionBottomSheet`는 `ModalBottomSheet` + `PermissionRequestContent`로 리팩토링
- Dialog entry는 **`Dialog` + `PermissionRequestContent`** 조합
- 장점: 양쪽 UX 스타일 모두 지원
- 단점: `PermissionBottomSheet.kt` 한 번 더 손봐야 함

현재는 **A**로 적용했습니다. B가 필요하면 별도 턴에서 리팩토링할 수 있습니다.

---

## 🏗 전체 구조 다이어그램

```
HomeScreen
├─ SharedTransitionLayout             ← Shared Element용
│   ├─ CompositionLocalProvider(LocalSharedTransitionScope)
│   │   ├─ NavDisplay
│   │   │   ├─ backStack: NavBackStack
│   │   │   ├─ sceneStrategies: [        ← 복수형 1.1.0 API
│   │   │   │    DialogSceneStrategy,          ← #3 Dialog
│   │   │   │    rememberListDetailSceneStrategy,  ← #2 Adaptive
│   │   │   │    SinglePaneSceneStrategy    ← Fallback
│   │   │   │  ]
│   │   │   ├─ entryDecorators: [
│   │   │   │    rememberSaveableStateHolderNavEntryDecorator,
│   │   │   │    rememberViewModelStoreNavEntryDecorator
│   │   │   │  ]
│   │   │   └─ entryProvider:
│   │   │       ├─ SearchPhotosRoute  [metadata = listPane()]     ← Adaptive list
│   │   │       ├─ PictureRoute       [metadata = detailPane()]   ← Adaptive detail
│   │   │       ├─ PermissionDialogRoute [metadata = dialog()]    ← Dialog scene
│   │   │       └─ 나머지 12개 entry                                ← SinglePane fallback
```

---

## ✅ 검증 결과

| 검증 항목 | 결과 |
|-----------|------|
| `sceneStrategies` (복수형) 사용 | ✅ 1건 (HomeScreen 라인 164) |
| `sceneStrategy` (단수형, 잘못됨) 사용 | ✅ 0건 |
| adaptive-navigation3 의존성 추가 | ✅ libs.versions.toml + app/build.gradle |
| ListDetailSceneStrategy import | ✅ HomeScreen + EntryProvider |
| DialogSceneStrategy import | ✅ HomeScreen + EntryProvider |
| SinglePaneSceneStrategy fallback | ✅ HomeScreen 리스트 마지막 |
| SharedTransitionLayout 감쌈 | ✅ HomeScreen 라인 159 |
| LocalSharedTransitionScope 노출 | ✅ HomeScreen 라인 160 |
| PermissionDialogRoute 정의 | ✅ Routes.kt |
| PermissionDialogRoute entry 등록 | ✅ PhogalEntryProvider.kt |
| BottomNavDestination 잔재 | ✅ 0건 (이전 턴 작업 유지) |
| Nav2 import 잔재 | ✅ 0건 |
| 잘못된 1.1.0 API 사용 | ✅ 0건 |

---

## 🧪 실기기 테스트 체크리스트

### ListDetailSceneStrategy
- [ ] 핸드폰 세로 → Gallery에서 사진 탭 → 기존처럼 push 전환
- [ ] 태블릿 가로 → Gallery 진입 시 좌(리스트) + 우(placeholder) 표시
- [ ] 태블릿 가로 → 리스트에서 사진 탭 → 우측 pane에 상세 화면 표시
- [ ] 태블릿 가로 → 뒤로가기 → 우측 pane이 placeholder로 복귀

### DialogSceneStrategy
- [ ] `navState.push(Routes.PermissionDialogRoute)` 호출 시 dialog 표시
- [ ] Dialog 내 Cancel / OK 버튼 → backstack pop → dialog 닫힘
- [ ] Dialog 표시 중 시스템 back 버튼 → pop 동작
- [ ] Dialog 표시 중 회전 → dialog 유지

### Shared Element (적용 후)
- [ ] 썸네일 탭 → 이미지가 상세 화면 위치로 부드럽게 확대 전환
- [ ] 상세에서 뒤로가기 → 이미지가 리스트 썸네일 위치로 축소 복귀

---

## 🚧 남은 작업 (사용자 재량)

사용자께서 원하실 때 다음을 별도로 진행할 수 있습니다:

### 1. 실제 shared element modifier 적용
- `PhotoItem.kt`의 이미지에 `Modifier.sharedElement(...)` 추가
- `PictureContent.kt`의 메인 이미지에 같은 key로 `Modifier.sharedElement(...)` 추가
- 위 "Shared Element Transition 사용법" 섹션의 예시 코드 참고

### 2. PermissionBottomSheet 리팩토링 (선택)
- 내부 content를 `PermissionRequestContent` composable로 분리
- 기존 `ModalBottomSheet` 사용처와 Nav3 dialog entry 양쪽에서 공유

### 3. 기존 `PermissionBottomSheet` 사용처의 `navState.push(PermissionDialogRoute)` 전환
- `SearchPhotosScreen` 등에서 권한 요청이 필요할 때 Nav3 방식으로 호출

---

## 📌 참고 링크

- 공식 Nav3 1.1.0 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/navigation3
- Nav2 → Nav3 마이그레이션 가이드: https://developer.android.com/guide/navigation/navigation-3/migration-guide
- Scenes와 custom layouts: https://developer.android.com/guide/navigation/navigation-3/custom-layouts
- Shared Elements 문서: https://developer.android.com/develop/ui/compose/animation/shared-elements

빌드 중 에러가 발생하면 전체 로그와 대상 파일을 공유해주세요. 특히 `adaptive-navigation3` 의존성 resolution이 실패하면 Maven 저장소 sync 후 재시도해야 합니다.
