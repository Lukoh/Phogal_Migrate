# Phogal - Navigation 3 마이그레이션 노트

작성 일자: 2026-04-19
대상: Navigation 3 **1.0.0 stable** (2025-11 출시)
호환 환경: AGP 8.7.3 / Kotlin 2.0.21 / Gradle 8.10.2 / Android Studio Panda 2

---

## 🎯 마이그레이션 전략: 병행 유지 (Coexistence)

이번 작업은 Nav2 코드를 **지우지 않고** Nav3를 **병행**으로 도입합니다.

```kotlin
// MainScreen.kt
private const val USE_NAV3: Boolean = false  // ← true로 바꾸면 전체 앱이 Nav3로 전환
```

### 왜 병행인가?

1. **Nav3에는 nested graph 개념이 없음** — 현재 4개 탭이 각각 `navigation<GalleryGraph>` 블록으로 감싼 구조인데, Nav3는 단일 flat entry provider + 탭별 backstack 직접 관리 방식. 구조 변경이 크기 때문에 운영 중인 앱이면 A/B 전환이 안전합니다.
2. **Multi-backstack을 직접 구현해야 함** — Nav2의 `saveState=true`/`restoreState=true` 조합이 하던 "탭 전환 시 각 탭의 마지막 위치 기억" 기능은 Nav3에선 프레임워크가 제공하지 않습니다. `PhogalNavState`로 직접 구현했습니다.
3. **ViewModel 스코프가 달라질 수 있음** — Nav3의 `rememberViewModelStoreNavEntryDecorator()`는 각 entry에 독립된 ViewModelStore를 제공합니다. 기존에 nested graph 루트 scope로 공유하던 ViewModel이 있다면 동작이 달라질 수 있으므로 실기기 테스트가 필요합니다.

두 경로 모두 **동일한 `Routes` 정의**(이제 `NavKey` 구현)를 공유하므로, 스크린/ViewModel/리포지토리 코드는 전혀 수정되지 않았습니다.

---

## 📁 추가/변경된 파일

### Gradle 설정 (2개)
| 파일 | 변경 |
|------|------|
| `gradle/libs.versions.toml` | Nav3 버전 + 라이브러리 정의 추가 |
| `app/build.gradle` | Nav3 의존성 3개 추가 |

### 소스 (5개)
| 파일 | 유형 | 역할 |
|------|------|------|
| `ui/navigation/Routes.kt` | 수정 | 모든 route가 `NavKey` 구현 |
| `ui/navigation/nav3/PhogalNavState.kt` | **신규** | 탭별 독립 backstack 매니저 |
| `ui/navigation/nav3/PhogalEntryProvider.kt` | **신규** | 모든 화면의 `entry<T>` 중앙 등록 |
| `ui/compose/screen/home/HomeScreenNav3.kt` | **신규** | `NavDisplay` 기반 홈 스크린 |
| `stateholder/uistate/MainScreenStateNav3.kt` | **신규** | Nav3용 MainScreenState |
| `ui/compose/screen/MainScreen.kt` | 수정 | `USE_NAV3` 토글 추가 |

### 보존된 Nav2 파일 (변경 없음)
- `ui/navigation/graph/GalleryNavGraph.kt`
- `ui/navigation/graph/PopularPhotosNavGraph.kt`
- `ui/navigation/graph/NotificationNavGraph.kt`
- `ui/navigation/graph/SettingNavGraph.kt`
- `ui/compose/screen/home/HomeScreen.kt`
- `stateholder/uistate/MainScreenState.kt`

---

## 🔍 핵심 API 매핑표

| 개념 | Nav2 | Nav3 |
|------|------|------|
| Backstack 진입점 | `rememberNavController()` | `rememberNavBackStack(...)` 또는 직접 관리 |
| Backstack 컨테이너 | `NavHost(navController, startDestination)` | `NavDisplay(backStack, ...)` |
| 화면 등록 | `composable<Route> { }` | `entry<Route> { key -> }` |
| 전진 | `navController.navigate(Route(...))` | `backStack.add(Route(...))` |
| 후진 | `navController.navigateUp()` | `backStack.removeLastOrNull()` |
| 파라미터 수신 | `backStackEntry.toRoute<Route>()` | `entry<Route> { key -> }`에서 `key`가 바로 데이터 |
| Nested graph | `navigation<Graph>(startDestination) { }` | **없음** (직접 관리) |
| 타입 안정성 | `@Serializable` | `@Serializable` + `NavKey` 구현 |
| ViewModel 스코프 | `hiltViewModel(backStackEntry)` | `hiltViewModel()` + `rememberViewModelStoreNavEntryDecorator()` |
| State 복원 | 프레임워크 자동 처리 | `rememberSavedStateNavEntryDecorator()` |

---

## 🧩 설계 포인트

### 1. `PhogalNavState` - Multi-backstack 매니저

```kotlin
class PhogalNavState {
    private val stacks: Map<BottomNavDestination, SnapshotStateList<NavKey>>
    var currentTab: BottomNavDestination
    val backStackForCurrentTab: SnapshotStateList<NavKey>

    fun selectTab(tab: BottomNavDestination) {
        if (currentTab == tab) popToTabRoot()  // 같은 탭 재탭: 루트로
        else currentTab = tab                    // 다른 탭: 메모리 상 백스택 유지
    }

    fun push(key: NavKey) { backStackForCurrentTab.add(key) }
    fun pop(): Boolean { /* ... */ }
}
```

**탭별 독립 백스택 구조**:
- `stacks[Gallery]`: `[SearchPhotosRoute, PictureRoute("xyz"), UserPhotosRoute(...)]`
- `stacks[Popular]`: `[PopularPhotosRoute]`
- `stacks[Setting]`: `[SettingRoute, BookmarkedPhotosRoute]`

사용자가 Gallery → Setting으로 전환했다가 다시 Gallery로 오면 `UserPhotosRoute`부터 보입니다. Nav2의 `saveState/restoreState`와 동일한 UX.

**프로세스 사망 대응**: `Saver`를 구현해서 `rememberSaveable`로 저장/복원됩니다.

### 2. `NavDisplay` 필수 Decorator 3개

```kotlin
NavDisplay(
    backStack = navState.backStackForCurrentTab,
    entryDecorators = listOf(
        rememberSceneSetupNavEntryDecorator(),      // 애니메이션 Scene 셋업
        rememberSavedStateNavEntryDecorator(),      // Config change 대응
        rememberViewModelStoreNavEntryDecorator()   // Hilt ViewModel 스코프
    ),
    entryProvider = entryProvider { phogalEntries(navState) }
)
```

**Decorator를 빠뜨리면**:
- `rememberSceneSetupNavEntryDecorator` 빠짐: 예측 가능한 뒤로가기 제스처 애니메이션 깨짐
- `rememberSavedStateNavEntryDecorator` 빠짐: 화면 회전 시 `rememberSaveable`로 저장된 상태 손실
- `rememberViewModelStoreNavEntryDecorator` 빠짐: 같은 타입의 entry가 여러 개일 때 ViewModel이 공유됨 (심각한 버그)

### 3. Entry Provider의 구조

Nav2에서는 4개 파일(`GalleryNavGraph.kt`, `PopularPhotosNavGraph.kt`, ...)에 분산되어 있었지만, Nav3에서는 하나의 `phogalEntries()` 함수에 모두 모았습니다. 파일이 흩어지는 것보다 한 곳에서 전체 라우트 맵을 보는 것이 유지보수가 쉽다는 판단입니다.

원한다면 `GalleryEntries.kt`, `SettingEntries.kt` 식으로 파일을 쪼개고 각각 `EntryProviderBuilder<NavKey>` 확장함수로 만든 뒤:
```kotlin
entryProvider {
    galleryEntries(navState)
    popularEntries(navState)
    settingEntries(navState)
}
```
로 합치는 것도 가능합니다. 이번에는 단순성을 위해 통합했습니다.

---

## ⚠️ Nav3 전환 시 주의사항

### 1. `BookmarkViewModel` / `FollowViewModel`의 공유 스코프

현재 이 ViewModel들은 Gallery tab과 Setting tab 양쪽에서 쓰입니다. Nav2에서는 `@HiltViewModel` + `hiltViewModel()`이 Activity scope로 올라가서 자연스럽게 공유됐습니다.

Nav3에서 `rememberViewModelStoreNavEntryDecorator()`를 적용하면 **entry 단위**로 ViewModelStore가 분리되어 같은 VM 타입이라도 다른 인스턴스가 됩니다.

**해결 방법**:
```kotlin
// 공유가 필요한 ViewModel은 Activity scope로 명시
val bookmarkVm: BookmarkViewModel = hiltViewModel(
    LocalContext.current as ViewModelStoreOwner
)
```

또는 Hilt `@ActivityRetainedScoped` 리포지토리에 상태를 두고 ViewModel은 매번 새로 만들어도 동일 데이터를 참조하게 하는 방식이 더 깔끔합니다.

### 2. `PopularPhotosScreen`, `NotificationsScreen` 시그니처

`PhogalEntryProvider.kt`에서 이들 스크린을 참조할 때 현재 시그니처 가정:
```kotlin
PopularPhotosScreen(onItemClicked = { id -> ... })
NotificationsScreen(onItemClicked = { id -> ... })
```

실제 프로젝트의 스크린 시그니처가 다르면 컴파일 에러가 납니다. 원래 `PopularPhotosNavGraph.kt`, `NotificationNavGraph.kt`를 확인하고 파라미터를 맞춰주세요. (기존 Nav2 그래프 파일을 참고해서 복사해 오는 것이 가장 빠름)

### 3. Deep link

Nav3 stable에서도 deep link는 Nav2 API와 다릅니다. 현재 프로젝트에 deep link 설정이 있는지 확인되지 않았는데, 있다면 별도 처리가 필요합니다.

### 4. 뒤로가기 제스처

Nav3는 Predictive Back을 기본 지원합니다. `NavDisplay(onBack = { count -> ... })`의 `count`는 연속 백 제스처에서 몇 단계 되돌릴지를 알려줍니다.

현재 구현:
```kotlin
onBack = { count ->
    repeat(count) { navState.pop() }
}
```
`pop()`이 false를 반환(이미 루트)하면 더 이상 pop하지 않고, `NavDisplay`가 상위로 이벤트를 전달해서 시스템이 앱을 종료합니다.

---

## 🚀 전환 테스트 절차

```bash
# 1. 현재 상태로 빌드 (Nav2)
./gradlew :app:assembleProdDebug
# → 앱 실행해 동작 확인

# 2. USE_NAV3 = true 로 변경
#    (MainScreen.kt 최상단)

# 3. Nav3로 빌드
./gradlew :app:assembleProdDebug
# → 앱 실행해 동작 비교:
#   - 탭 전환 시 각 탭의 이전 위치 기억 여부
#   - 뒤로가기 동작 (연속 백 포함)
#   - 화면 회전 시 상태 유지
#   - Bookmark/Follow ViewModel 데이터 공유 여부 ← 주의
```

---

## 🔮 다음 단계 제안

**단기**
- `BookmarkViewModel`/`FollowViewModel`을 `@ActivityRetainedScoped` 리포지토리 + 경량 VM 구조로 리팩터링
- `PopularPhotosScreen`/`NotificationsScreen` 실제 시그니처에 맞춰 entryProvider 조정
- 실기기 테스트 후 `USE_NAV3` 기본값 `true`로 전환

**중기 (Nav3 전용 최적화)**
- `Adaptive Layout` — Nav3의 `ListDetailSceneStrategy` 도입 (태블릿/폴더블에서 마스터-디테일 자동 전환)
- `Shared Element Transition` — `NavDisplay(sharedTransitionScope = ...)`로 사진 상세 화면 진입 시 히어로 애니메이션
- Nav2 코드(graph 파일 4개 + `HomeScreen.kt` + `MainScreenState.kt`) 제거

**장기**
- deep link 처리 (Nav3 방식)
- Single Activity 다중 네비게이션 트리 (예: 로그인 플로우 별도 `NavDisplay`)

---

## 📊 예상 이점 요약

| 항목 | 효과 |
|------|------|
| **Boilerplate** | `NavController`, `NavGraphBuilder`, `backStackEntry.toRoute()` 제거 |
| **타입 안정성** | `NavKey` 구현으로 컴파일 타임 보장 더 강함 |
| **Multi-backstack 제어권** | 프레임워크 블랙박스에서 벗어나 상태 직접 관리 |
| **Adaptive Layout** | `SceneStrategy` 교체만으로 태블릿 대응 가능 |
| **Compose 친화적** | State-driven, 관찰 가능한 backstack |
| **예측 가능성** | 뒤로가기, Scope, 상태 복원이 모두 명시적 |

---

질문이 있거나 특정 화면에서 Nav3 entry 등록이 실패하면 에러 로그와 함께 알려주세요.
