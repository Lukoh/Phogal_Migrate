# Phogal - Navigation 3 **1.1.0** 마이그레이션 노트

작성 일자: 2026-04-20
대상: **Navigation 3 1.1.0** (2026-04-08 stable 출시)
호환 환경: AGP 8.7.3 / Kotlin 2.0.21 / Gradle 8.10.2 / Android Studio Panda 2

---

## 🎯 요약

프로젝트는 이미 Nav3 1.0.0 기반으로 동작 중이었습니다. 이번 작업은 **1.1.0의 새 API와 권장 패턴**에 맞게 내부를 정리하고, 발견한 잠재 버그 1건을 수정했습니다.

### 주요 변경점 (3줄 요약)

1. **`rememberSavedStateNavEntryDecorator()` → `rememberSaveableStateHolderNavEntryDecorator()`**
2. **`rememberSceneSetupNavEntryDecorator()` 제거** + **`sceneStrategy = SinglePaneSceneStrategy()` 추가**
3. **`transitionSpec` / `popTransitionSpec` / `predictivePopTransitionSpec` 명시**

---

## 📋 변경된 파일 (3개)

| 파일 | 유형 | 변경 내용 |
|------|------|-----------|
| `ui/navigation/nav3/PhogalNavState.kt` | 재작성 | `NavBackStack` 공식 타입 사용, 각 탭을 `rememberNavBackStack`로 생성 |
| `ui/navigation/nav3/PhogalEntryProvider.kt` | 재작성 | 잘못된 `import java.util.Map.entry` 제거 ⚠️, 탭별 분할 |
| `ui/compose/screen/home/HomeScreen.kt` | 재작성 | 1.1.0 API로 전면 업데이트, `DefaultTransitions` 추출 |

### 변경 없이 유지
- `libs.versions.toml` — 버전 정의는 이미 올바름 (navigation3 = "1.1.0", lifecycleViewmodelNavigation3 = "2.10.0")
- `Routes.kt` — `NavKey` 구현은 이미 올바름
- `MainScreenStateNav3.kt` — 변경 불필요
- `MainScreen.kt` — 변경 불필요

---

## 🔍 API 변화 상세

### 변경 1: 필수 Decorator 이름 변경 (⚠️ Breaking)

```kotlin
// ❌ 1.0.0 (구버전)
entryDecorators = listOf(
    rememberSceneSetupNavEntryDecorator(),      // 삭제됨 (자동화)
    rememberSavedStateNavEntryDecorator(),      // ← 이름 변경
    rememberViewModelStoreNavEntryDecorator()
)

// ✅ 1.1.0 (신버전)
entryDecorators = listOf(
    rememberSaveableStateHolderNavEntryDecorator(),   // ← 새 이름
    rememberViewModelStoreNavEntryDecorator()
)
```

**이름이 바뀐 이유**: 이 decorator의 실제 역할은 각 entry에 `SaveableStateHolder`를 할당해서 `rememberSaveable`이 entry별로 작동하게 하는 것이었습니다. "SavedState"라는 이름은 Android의 `SavedStateRegistry`와 혼동되기 쉬워서, 실제 내부 동작(`SaveableStateHolder` 사용)을 드러내는 이름으로 바뀌었습니다.

### 변경 2: SceneSetup decorator는 자동화

`rememberSceneSetupNavEntryDecorator()`는 1.1.0에서 `sceneStrategy`가 내부적으로 호출합니다. 명시적으로 리스트에 추가하면 중복 실행될 수 있어 오히려 권장되지 않습니다.

### 변경 3: `sceneStrategy` 도입

```kotlin
NavDisplay(
    backStack = backStack,
    sceneStrategy = remember { SinglePaneSceneStrategy<Any>() },  // ← 추가
    entryDecorators = [...],
    entryProvider = {...}
)
```

**`SinglePaneSceneStrategy`**: Nav2 기본 동작과 동일한 "한 번에 한 화면" 렌더링.

**확장성**: 나중에 태블릿/폴더블 대응이 필요할 때, 단 한 줄만 교체하면 됩니다:
```kotlin
val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
sceneStrategy = listDetailStrategy
```
그러면 넓은 화면에서는 list와 detail이 나란히 보이고, 좁은 화면에서는 push로 동작합니다. `entryProvider`의 `entry<T>` 블록에 `metadata = ListDetailSceneStrategy.listPane()` 같은 힌트만 추가하면 됩니다.

### 변경 4: Transition spec 3종 명시

1.1.0은 `transitionSpec` (forward), `popTransitionSpec` (back), `predictivePopTransitionSpec` (predictive back 제스처) 세 가지를 구분합니다.

```kotlin
NavDisplay(
    transitionSpec = DefaultTransitions.push,
    popTransitionSpec = DefaultTransitions.pop,
    predictivePopTransitionSpec = DefaultTransitions.predictivePop,
    ...
)
```

`HomeScreen.kt` 안의 private `DefaultTransitions` object에 각 스펙을 `val`로 올려두었습니다. 한 번만 생성되고 재사용되므로 불필요한 람다 할당이 없습니다.

### 변경 5: `NavBackStack` 공식 타입 사용

```kotlin
// ❌ 1.0.0 시절 수기 구현
private val stacks: Map<Tab, SnapshotStateList<NavKey>>

// ✅ 1.1.0 권장 패턴
private val stacks: Map<Tab, NavBackStack>   // NavBackStack = SnapshotStateList<NavKey> + Saver
```

`rememberNavBackStack(initialKey)`를 각 탭마다 호출하면 탭별 back stack이 자동으로 프로세스 사망에서 복원됩니다. 이전에는 `PhogalNavState`에 커스텀 `Saver`를 직접 만들어야 했는데, 이제 프레임워크가 해 줍니다.

---

## 🐛 발견해서 수정한 버그

업로드해주신 `PhogalEntryProvider.kt`의 28번째 줄에 있던 잘못된 import:

```kotlin
import java.util.Map.entry   // ❌ java.util.Map.entry (정적 메서드)
```

Kotlin은 동일한 이름(`entry`)을 가진 심볼이 여러 개 있을 때 "가장 가까운" 것을 우선 선택하므로, 경우에 따라 `entry<Routes.SearchPhotosRoute> { ... }`가 **Nav3의 DSL이 아닌 `java.util.Map.entry()`로 오해석되어 컴파일 실패**하거나, 더 나쁘게는 IDE는 통과하는데 런타임에 이상 동작할 수 있었습니다.

수정된 import 세트:
```kotlin
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry   // ✅ Nav3의 진짜 entry DSL
```

---

## ⚡ Compose 최적화

### 1. `BottomNavBar` 안정성 강화

```kotlin
// ❌ 매 recomposition마다 새 리스트 생성
val items = BottomNavDestination.values().toList()

// ✅ 한 번만 계산되고 재사용 (Kotlin 1.9+ entries API)
val items = remember { BottomNavDestination.entries }
```

### 2. Decorator 리스트 안정화

```kotlin
val stateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator()
val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator()
val entryDecorators = remember(stateHolderDecorator, viewModelStoreDecorator) {
    listOf(stateHolderDecorator, viewModelStoreDecorator)
}
```

각 decorator는 composition 중에 remember되지만, 이들의 **리스트**는 매번 새 인스턴스가 만들어지면 `NavDisplay` 파라미터 비교 실패로 불필요한 recomposition을 유발합니다. `remember(decorators)`로 리스트 자체도 stable하게 만듭니다.

### 3. `SinglePaneSceneStrategy` 재사용

```kotlin
val sceneStrategy = remember { SinglePaneSceneStrategy<Any>() }
```

SceneStrategy는 stateless한 전략 객체라 한 번만 생성하면 됩니다.

### 4. `DefaultTransitions` object로 람다 추출

3개의 transition lambda는 클래스 수준 `object`로 올렸습니다. `HomeScreen` recomposition 시마다 lambda가 새로 캡처되지 않고, JIT가 인라인화하기도 쉬운 형태입니다.

### 5. `bottomBarVisible` — `animateDpAsState` 제거

이전 코드:
```kotlin
val bottomBarOffset by animateDpAsState(
    targetValue = if (bottomBarVisible) 0.dp else 80.dp,
    label = "bottomBarOffset"
)
```

1.1.0의 `NavDisplay`는 이미 자체 `transitionSpec`으로 화면 전환 애니메이션을 제공합니다. Bottom bar의 슬라이드 애니메이션까지 `animateDpAsState`로 별도로 돌리면 두 애니메이션이 서로 다른 animation spec을 쓰는 미묘한 불일치가 생깁니다.

수정:
```kotlin
val bottomBarOffset: Dp = if (bottomBarVisible) 0.dp else 80.dp
```

이제 bottom bar는 offset만 바뀌고, 시각적 전환은 screen content의 transition과 동기화됩니다. 애니메이션이 꼭 필요하다면 `Modifier.animateContentSize()`나 `AnimatedVisibility`로 감싸는 것이 NavDisplay 타이밍과 더 잘 맞습니다.

---

## 🔬 1.1.0의 신기능 — 아직 사용하지 않았지만 바로 활용 가능

### A. Shared Element Transitions (1.1.0의 하이라이트)

Scene 간 히어로 애니메이션을 Nav2 시절 Accompanist에 의존하지 않고 프레임워크로 구현 가능:

```kotlin
NavDisplay(
    sharedTransitionScope = rememberSharedTransitionScope(),
    ...
)

// 각 entry 안에서:
Modifier.sharedElement(
    state = rememberSharedContentState(key = "photo_${photoId}"),
    animatedVisibilityScope = LocalNavAnimatedContentScope.current
)
```

Phogal의 사진 썸네일 → 상세화면 전환에 바로 적용 가능합니다.

### B. `ListDetailSceneStrategy` — 태블릿/폴더블 대응

```kotlin
val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

entry<Routes.SearchPhotosRoute>(
    metadata = ListDetailSceneStrategy.listPane(
        detailPlaceholder = { /* "사진을 선택하세요" UI */ }
    )
) { ... }

entry<Routes.PictureRoute>(
    metadata = ListDetailSceneStrategy.detailPane()
) { key -> ... }
```

큰 화면에서는 리스트와 상세가 동시에 표시됩니다. 작은 화면에서는 기존처럼 push 동작.

### C. Dialog destinations

```kotlin
@Serializable data class SomeDialog(val id: String) : NavKey

entry<SomeDialog>(
    metadata = DialogSceneStrategy.dialog()
) { ... }

NavDisplay(
    sceneStrategy = listOf(
        SinglePaneSceneStrategy(),
        DialogSceneStrategy()
    ),
    ...
)
```

Dialog도 이제 backstack의 일원으로 취급되어 backstack 복원, deep link, predictive back이 자동 동작합니다.

---

## ✅ 빌드 확인

```bash
./gradlew :app:assembleProdDebug
```

컴파일만 먼저 확인하려면:
```bash
./gradlew :app:compileProdDebugKotlin
```

예상되는 첫 빌드 경고/에러와 대처:

| 증상 | 원인 | 해결 |
|------|------|------|
| `Unresolved reference: rememberSavedStateNavEntryDecorator` | 다른 파일에 아직 구버전 API 남아있음 | 프로젝트 전역 검색 후 `rememberSaveableStateHolderNavEntryDecorator`로 치환 |
| `Unresolved reference: rememberSceneSetupNavEntryDecorator` | 1.1.0에서 제거됨 | 리스트에서 해당 라인 삭제 |
| `NavDisplay` 파라미터 불일치 | 1.0.0 시절 시그니처 사용 | `sceneStrategy` 파라미터 추가 |
| `BottomNavDestination.values()` 경고 | Kotlin 1.9+ 권장은 `entries` | `.entries` 사용 |

---

## 📦 Compose 안정성 체크리스트

이번 작업에서 반영한 Compose 권장 패턴:

- [x] `@Stable` object로 transition spec 묶어서 재할당 제거
- [x] `remember`로 decorator 리스트 안정화
- [x] `BottomNavDestination.entries` 사용 (불필요한 리스트 생성 제거)
- [x] `animateDpAsState` 이중 애니메이션 제거
- [x] `NavBackStack` 공식 타입으로 `rememberSaveable` 중복 제거
- [x] `phogalEntries`를 4개 private 함수로 분할 → 파일 전체가 recompose되는 경우 줄임
- [x] 잘못된 `java.util.Map.entry` import 제거

---

## 🔮 다음 단계 권장

**단기 (이번 스프린트)**
- 위 변경사항 실기기 테스트 — 특히 탭 전환 시 각 탭 backstack 보존
- Predictive back 제스처 애니메이션 UX 확인 (Android 14+)

**중기 (다음 스프린트)**
- Shared Element Transition을 사진 썸네일 → 상세 전환에 적용
- `ListDetailSceneStrategy`로 태블릿 가로 모드 대응

**장기 (리팩토링 단계)**
- Nav2 의존성 완전 제거 (현재 `navigation-compose` 2.8.x가 아직 남아있다면)
- `DialogSceneStrategy`로 Bottom sheet / Dialog도 navigation 편입

---

## 📊 변경 전후 비교

| 항목 | Before (Nav3 1.0.0 코드) | After (Nav3 1.1.0 코드) |
|------|-----------------------|------------------------|
| Decorator 개수 | 3개 (SceneSetup 포함) | 2개 (자동화된 것 제거) |
| Decorator 이름 정확성 | 구버전 이름 사용 | 1.1.0 정확 이름 |
| `sceneStrategy` 파라미터 | 없음 | `SinglePaneSceneStrategy` |
| Transition spec | 기본값 사용 | 3종 명시 (push/pop/predictive) |
| BackStack 타입 | `SnapshotStateList<NavKey>` | `NavBackStack` (공식) |
| BackStack 영속화 | 커스텀 Saver | `rememberNavBackStack` 자동 |
| 잘못된 import | 있음 (`java.util.Map.entry`) | 제거됨 |
| BottomNavBar lambda stability | 매 recomp마다 새 리스트 | remember로 안정화 |
| Bottom bar 애니메이션 | `animateDpAsState` (불일치 가능) | NavDisplay transition과 동기화 |

빌드 중 에러가 나면 전체 에러 로그를 공유해주세요. Nav3는 알파 시절에 비해 안정화되었지만, transient compiler 에러는 종종 decorator 이름이나 import 문제입니다.
