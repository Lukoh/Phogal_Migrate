# Phogal — 2023 → 2026 Modernization Journey

<p align="left">
  <a href="#"><img alt="Android OS" src="https://img.shields.io/badge/OS-Android-3DDC84?style=flat-square&logo=android"></a>
  <a href="#"><img alt="Language-Kotlin" src="https://img.shields.io/badge/Kotlin-2.0.21-f18e33?style=flat-square&logo=kotlin"></a>
  <a href="#"><img alt="Compose" src="https://img.shields.io/badge/Compose_BOM-2026.3.01-4285F4?style=flat-square"></a>
  <a href="#"><img alt="Navigation" src="https://img.shields.io/badge/Navigation_3-1.1.0_stable-00897B?style=flat-square"></a>
  <a href="#"><img alt="JDK" src="https://img.shields.io/badge/JDK-17-red?style=flat-square"></a>
  <a href="#"><img alt="PRs" src="https://img.shields.io/badge/PRs-Welcome-3DDC84?style=flat-square"></a>
</p>

> **한 줄 요약**  
> 2023년 8월에 출시된 Phogal을 약 2년 6개월 만에 다시 점검하여, **Kotlin 2.0 Compose Compiler Plugin**, **Navigation 3 1.1.0 (stable)**, **Material 3 Adaptive**, **Shared Element Transition** 등 2026년 현재 Android 생태계의 **최신 권장 스택**으로 전면 Migration(현대화한) 프로젝트 기록입니다. 단순 업그레이드가 아니라 **기술부채를 정리하고 장기 유지보수성을 끌어올리는** 데 초점을 두었습니다.

---

<p align="left">
:eyeglasses: Phogal by open-source contributor, Lukoh.
</p><br>

![header](https://1.bp.blogspot.com/-9MiK78CFMLM/YQFurOq9AII/AAAAAAAAQ1A/lKj5GiDnO_MkPLb72XqgnvD5uxOsHO-eACLcBGAsYHQ/s0/Android-Compose-1.0-header-v2.png)

# Phogal 
## Better Android Apps Using latest advanced Android Architecture Guidelines + Dependency injection with Hilt + Jetpack Compose + Navigation(Navigating3 with Compose). Using Android Architecture Guidelines

[Here is the demo vidoe.](https://youtu.be/VGliiemyZ20)
[Here is the demo vidoe.](https://youtube.com/shorts/Z7qvPz3-ICg?feature=share)


<img src="https://github.com/Lukoh/Phogal/blob/main/screenshot.png" data-canonical-src="https://youtu.be/U_mvFoxypjM" width="220" height="450" /> <img src="https://github.com/Lukoh/Phogal/blob/main/Shot1.png" data-canonical-src="https://youtu.be/U_mvFoxypjM" width="220" height="450" /> <img src="https://github.com/Lukoh/Phogal/blob/main/shot2.png" data-canonical-src="https://youtu.be/U_mvFoxypjM" width="220" height="450" /> <img src="https://github.com/Lukoh/Phogal/blob/main/shot3.png" data-canonical-src="https://youtu.be/U_mvFoxypjM" width="220" height="450" /> <img src="https://github.com/Lukoh/Phogal/blob/main/shot4.png" data-canonical-src="https://youtu.be/U_mvFoxypjM" width="220" height="450" /> <img src="https://github.com/Lukoh/Phogal/blob/main/shot5.png" data-canonical-src="https://youtu.be/U_mvFoxypjM" width="220" height="450" /> <img src="https://github.com/Lukoh/Phogal/blob/main/Bookmark_shot1.png" data-canonical-src="https://youtu.be/U_mvFoxypjM" width="220" height="450" /> <img src="https://github.com/Lukoh/Phogal/blob/main/Bookmark_shot2.png" data-canonical-src="https://youtu.be/U_mvFoxypjM" width="220" height="450" /> <img src="https://github.com/Lukoh/Phogal/blob/main/Popular.png" data-canonical-src="https://youtu.be/U_mvFoxypjM" width="220" height="450" />


## 📑 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [버전 비교 요약](#2-버전-비교-요약-표-한-장)
3. [소스 구조 비교](#3-소스-구조-비교)
4. [최신 Compose 기술 적용](#4-최신-compose-기술-적용)
5. [Navigation 3 라이브러리 적용](#5-navigation-3-라이브러리-적용)
6. [최신 아키텍처 구조](#6-최신-아키텍처-구조)
7. [기술부채 해소 — 개선한 구조와 방향성](#7-기술부채-해소--개선한-구조와-방향성)
8. [마이그레이션 타임라인](#8-마이그레이션-타임라인)
9. [향후 로드맵](#9-향후-로드맵)

---

## 1. 프로젝트 개요

**Phogal**은 Unsplash API를 기반으로 한 사진 탐색·북마크 Android 앱으로, 원래 "최신 Android 공식 가이드라인을 실전에서 어떻게 적용하는가"를 보여주는 **레퍼런스 프로젝트** 성격으로 시작되었습니다.

- **2023년 8월 버전** — 당시 기준 최신이었던 Kotlin 1.8.21, Compose BOM 2023.04.01, Navigation 2 기반으로 Clean Architecture + MVVM + Hilt를 실전 적용
- **2026년 4월 버전** — Kotlin 2.0.21, Compose BOM 2026.3.01, **Navigation 3 1.1.0 stable**, Material 3 Adaptive까지 포함한 2026년 4월 현재의 공식 권장 스택 전면 반영

> 레퍼런스 프로젝트가 *스스로의 역할을 다시 수행하기 위해* 시대에 맞게 다시 태어났다 — 이것이 이 현대화의 핵심 동기입니다.

---

## 2. 버전 비교 요약 (표 한 장)

| 항목 | **2023년 8월** | **2026년 4월** | 변화의 의미 |
|------|------|------|------|
| **Kotlin** | 1.8.21 | **2.0.21** | K2 컴파일러 기본, 컴파일 속도 대폭 개선 |
| **Compose Compiler** | `kotlinCompilerExtensionVersion = "1.4.7"` (KotlinCompilerExtension) | **Kotlin Compose Compiler Plugin** (`org.jetbrains.kotlin.plugin.compose`) | Kotlin과 버전 짝맞추는 악몽 종료 |
| **Compose BOM** | 2023.04.01 | **2026.3.01** | 36개 라이브러리 버전 자동 정렬 |
| **Material 3** | 초기 1.x | **1.3.1** + **Material 3 Adaptive** | 태블릿/폴더블 공식 지원 |
| **Navigation** | **Nav 2** (NavHostController + String route) | **Nav 3 1.1.0 stable** (NavBackStack + typed NavKey) | 네비게이션 패러다임 변경 |
| **Dialog 네비게이션** | Compose-state(`remember`) | **`DialogSceneStrategy`** (backstack 편입) | 회전/프로세스 사망에도 복원 |
| **List-Detail 레이아웃** | 직접 구현 필요 | **`ListDetailSceneStrategy`** | 태블릿에서 자동 2-pane |
| **Shared Element** | Compose 1.7에서 도입 | **Nav 3 + `SharedTransitionLayout`** 통합 | 목적지 전환시 hero 애니메이션 |
| **Hilt** | 2.x 초기 | **2.58** (+ `hilt-navigation-compose` 1.3.0) | Kotlin 2.0 + KSP 2.0.21 호환 |
| **DI 어노테이션 처리** | **kapt** | **KSP 2.0.21-1.0.28** | Kotlin 2.0 전용, 2~3배 빠름 |
| **의존성 관리** | `build.gradle` 직접 기재 | **Version Catalog** (`libs.versions.toml`) | 단일 진실원, 타입 안전 |
| **Tab Destination** | `enum class BottomNavDestination` | **`sealed interface BottomNavRoute : NavKey`** | 탭 identity 자체가 NavKey |
| **Deep-link 준비** | String route 기반 | **`@Serializable` + NavKey** | KMP/Deep-link 미리 대비 |
| **compileSdk** | 34 | **36** | Android 15 API level |
| **targetSdk** | 34 | **36** | — |
| **JDK** | 11 | **17** | Modern Gradle/AGP 요구 |
| **AGP** | 8.0 계열 | **8.13.2** | 최신 빌드 최적화 |
| **.kt 파일 수** | 189 | **177** | 12개 감소 — 중복 제거 + 통합 |

---

## 3. 소스 구조 비교

### 3.1 navigation 패키지 — **가장 극적인 변화**

#### 🔴 Before (2023) — Nav2 기반, 10개 파일

```
presentation/ui/navigation/
├── destination/                           ← 각 화면별 "PhogalDestination" 구현체
│   ├── PhogalDestination.kt              ← String route 상수 집합
│   ├── Gallery.kt                        ← SearchPhotos + Picture + UserPhotos + WebView 묶음
│   ├── PopularPhotos.kt
│   ├── Notification.kt
│   └── Setting.kt
├── graph/                                 ← Nav2 중첩 graph DSL
│   ├── GalleryNavGraph.kt                ← navigation<T>(...) { composable(...) }
│   ├── PopularPhotosNavGraph.kt
│   ├── NotificationNavGraph.kt
│   └── SettingNavGraph.kt
└── ext/
    └── NavHostControllerExt.kt            ← navigateSingleTopToGraph 등 확장
```

**특징** — 타입 안전성 없음, 중첩 graph 관리 복잡, `.popBackStack()` / `.navigate(...)` 분기 로직이 scattered.

#### 🟢 After (2026) — Nav3 기반, 4개 파일

```
presentation/ui/navigation/
├── Routes.kt                              ← @Serializable data class/object, 모두 NavKey
└── nav3/
    ├── NavigationState.kt                 ← Multi-backstack 관리 + per-tab persistence
    ├── PhogalEntryProvider.kt             ← EntryProviderScope<NavKey>.phogalEntries(...)
    └── SharedTransitionKeys.kt            ← LocalSharedTransitionScope + hero key helper
```

**특징** — 모든 route가 컴파일 타임 타입 안전, backstack은 그냥 `SnapshotStateList`, 화면 전환은 `backStack.add(RouteKey(...))` 하나로 통일.

**수치로 본 감소폭**: 10개 파일 → 4개 파일 (**-60%**), navigation 관련 LOC 약 40% 감소.

### 3.2 compose/screen 패키지 — 크게 안정적

화면 자체의 위계는 거의 동일합니다. 이미 Clean Architecture 분리가 잘 되어 있어 **UI 레이어는 최소한의 변경**으로 Nav3에 적응 가능했습니다. 주요 변경점:

```diff
  presentation/ui/compose/screen/home/
+   BottomNavRoute.kt         ← 🆕 sealed interface (탭 identity + NavKey)
    HomeScreen.kt              ← Scaffold + NavDisplay (NavHost 교체)
    OfflineScreen.kt
    common/
      photo/PhotoItem.kt       ← (옵션) shared element modifier 지점
      photo/PictureContent.kt  ← (옵션) shared element modifier 지점
      ...
```

---

## 4. 최신 Compose 기술 적용

### 4.1 Kotlin 2.0 Compose Compiler Plugin

2023년엔 Compose BOM과 Kotlin 버전이 **서로 다른 주기로** 업데이트되어 "어떤 BOM이 어떤 Kotlin에 호환되는가"가 끝없는 숙제였습니다. Kotlin 2.0부터 도입된 **Compose Compiler Gradle Plugin**으로 이 문제가 사라졌습니다.

```kotlin
// 2023 build.gradle (더 이상 권장되지 않음)
composeOptions {
    kotlinCompilerExtensionVersion = "1.4.7"   // ← 사용 중인 Kotlin 버전과 직접 매칭
}

// 2026 build.gradle — 단순한 plugin 선언 한 줄
plugins {
    alias(libs.plugins.kotlin.compose)   // REQUIRED for Kotlin 2.0+
}
```

**효과**: Kotlin을 올릴 때 Compose Compiler 버전을 따로 맞출 필요가 없어져 **업그레이드 리스크가 크게 감소**합니다.

### 4.2 Compose BOM 2026.3.01

36개 이상의 Compose 라이브러리(`ui`, `foundation`, `material3`, `animation`, `runtime`, …)가 BOM에 포함되어 **버전을 하나만** 관리합니다.

```kotlin
implementation(platform("androidx.compose:compose-bom:2026.3.01"))
implementation("androidx.compose.material3:material3")      // 버전 없음 — BOM이 결정
implementation("androidx.compose.animation:animation")      // 버전 없음 — BOM이 결정
```

### 4.3 `SharedTransitionLayout` + hero 애니메이션

Compose 1.7(2024)에서 stable이 된 **Shared Element Transition**이 Nav3 1.1.0과 공식 통합되어, 목적지(Scene) 경계를 넘나드는 hero 애니메이션이 가능해졌습니다.

```kotlin
SharedTransitionLayout {
    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
        NavDisplay(
            // ...
            // LocalNavAnimatedContentScope는 Nav3가 각 entry 안에서 자동 제공
        )
    }
}
```

**2023 vs 2026 UX**: 목록에서 사진을 탭하면 썸네일이 상세 화면의 전면 이미지 위치로 **부드럽게 확장** 전환. 기존에는 단순 슬라이드 전환이었습니다.

### 4.4 Material 3 Adaptive

Compose Material 3의 adaptive 서브모듈로 **폰/태블릿/폴더블** 레이아웃 자동 전환:

```kotlin
val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

NavDisplay(
    sceneStrategies = listOf(
        DialogSceneStrategy<NavKey>(),
        listDetailStrategy,                       // ← 자동 1-pane/2-pane
        SinglePaneSceneStrategy<NavKey>()
    ),
    // ...
)
```

각 entry는 `metadata`로 list/detail 역할을 선언:

```kotlin
entry<Routes.SearchPhotosRoute>(
    metadata = ListDetailSceneStrategy.listPane(
        detailPlaceholder = { DetailPlaceholder() }
    )
) { /* list screen */ }

entry<Routes.PictureRoute>(
    metadata = ListDetailSceneStrategy.detailPane()
) { /* detail screen */ }
```

**효과**: 태블릿 사용자는 좌측(목록) + 우측(상세)를 동시에 보게 되어 태블릿 UX가 처음부터 "Material Design 적합"으로 만들어집니다. 2023 버전에서는 직접 구현해야 했습니다.

### 4.5 `WindowSizeClass` 기반 UI 분기

```kotlin
val shouldShowBottomBar: Boolean
    get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
```

Compact(폰)에서만 Bottom Navigation Bar를 표시하고, Medium/Expanded에서는 숨기거나 NavigationRail로 교체할 수 있는 기반 마련.

### 4.6 Lifecycle 통합 `collectAsStateWithLifecycle`

```kotlin
val isOffline by state.isOffline.collectAsStateWithLifecycle()
```

2023 버전에서 `collectAsState()`를 사용하던 지점들을 `collectAsStateWithLifecycle()`로 전환 — 백그라운드 상태에서 **불필요한 collection이 중단**되어 배터리 효율 개선.

---

## 5. Navigation 3 라이브러리 적용

### 5.1 Nav 2 → Nav 3 패러다임 전환

| 개념 | Nav 2 (2023) | Nav 3 1.1.0 (2026) |
|------|------|------|
| **컨트롤러** | `NavHostController` (Stateful 객체) | `NavBackStack` (`SnapshotStateList<NavKey>`) |
| **라우트 정의** | `const val route = "picture/{id}"` 문자열 | `@Serializable data class PictureRoute(val id: String)` |
| **파라미터 전달** | `navArgument("id") { type = NavType.StringType }` | 직접 typed property 접근 (`key.id`) |
| **중첩 그래프** | `navigation<T>(...) { composable(...) }` | **없음** (탭 구조는 Multi-backstack으로 표현) |
| **push** | `navController.navigate("picture/abc")` | `backStack.add(PictureRoute(id = "abc"))` |
| **pop** | `navController.popBackStack()` | `backStack.removeLastOrNull()` |
| **Scene 확장** | 커스텀 NavHost 필요 | `SceneStrategy` 여러 개 조합 (Dialog/ListDetail/…) |
| **Dialog** | 별도 Compose state로 관리 | `DialogSceneStrategy`로 backstack 편입 |
| **Shared Element** | 별도 작업 필요 | `SharedTransitionLayout` + NavDisplay 통합 |

### 5.2 Route 정의 — 문자열에서 타입으로

**Before (2023)**:
```kotlin
object PhogalDestination {
    internal const val searchPhotosStartRoute = "photoHome/searchPhotos"
    internal const val pictureRouteArgs = "photoHome/picture/{id}/{showViewPhotosButton}"
}

// 네비게이션 시 문자열 조립
navController.navigate("photoHome/picture/$id/$showButton")

// 파라미터 꺼낼 때 NavType 정의 필요
arguments = listOf(
    navArgument("id") { type = NavType.StringType },
    navArgument("showViewPhotosButton") { type = NavType.BoolType }
)
```

**After (2026)**:
```kotlin
@Serializable
data class PictureRoute(
    val id: String,
    val showViewPhotosButton: Boolean
) : NavKey

// 네비게이션 시 직접 인스턴스 전달 — 컴파일 타임 타입 안전
navState.push(Routes.PictureRoute(id = photoId, showViewPhotosButton = true))

// 파라미터 꺼낼 때 그냥 property 접근
entry<Routes.PictureRoute> { key ->
    PictureScreen(photoId = key.id, showButton = key.showViewPhotosButton)
}
```

**효과**:
- 오타로 인한 런타임 크래시 제거 (`"piture/..."` 오타 → 컴파일 단계에서 잡힘)
- 파라미터 타입/누락 여부를 IDE가 즉시 경고
- `@Serializable`로 프로세스 사망 복원 자동 지원

### 5.3 Multi-backstack — 탭별 독립 스택

2023 Nav2 버전은 한 `NavHostController`에 중첩 `navigation<T>(...)` 그래프 4개로 탭을 표현했습니다. Nav3에서는 **탭 4개 = NavBackStack 4개**로 더 직관적인 구조:

```kotlin
// NavigationState.kt (현대판)
class NavigationState internal constructor(
    startRoute: BottomNavRoute,
    private val stacks: Map<BottomNavRoute, NavBackStack<NavKey>>,
) {
    val currentRoute: BottomNavRoute       // 현재 선택된 탭
    val backStackForCurrentRoute: NavBackStack<NavKey>   // 그 탭의 스택

    fun selectTab(tab: BottomNavRoute) { ... }
    fun push(key: NavKey) { ... }
    fun pop(): Boolean { ... }
}
```

**효과**:
- 탭 전환 시 이전 탭의 스택이 온전히 보존됨 (Material 표준 "탭은 자기 히스토리를 기억한다")
- 각 탭이 `rememberNavBackStack`으로 **프로세스 사망 복원까지 무료**로 지원

### 5.4 탭 identity 자체가 NavKey — `sealed interface BottomNavRoute`

2023 버전의 `enum class BottomNavDestination`을 2026 버전에서 `sealed interface BottomNavRoute : NavKey`로 승격:

```kotlin
@Serializable
sealed interface BottomNavRoute : NavKey {
    @get:DrawableRes val icon: Int
    @get:StringRes val title: Int

    @Serializable data object Gallery : BottomNavRoute {
        override val icon: Int get() = R.drawable.ic_photo
        override val title: Int get() = R.string.bottom_navigation_gallery
    }
    @Serializable data object PopularPhotos : BottomNavRoute { ... }
    @Serializable data object Notification : BottomNavRoute { ... }
    @Serializable data object Setting : BottomNavRoute { ... }

    companion object {
        val entries: List<BottomNavRoute> = listOf(Gallery, PopularPhotos, Notification, Setting)
    }
}
```

**enum 대비 이점**:
1. **특정 탭만 파라미터를 가질 수 있음** — 예: `data class Setting(val userId: String)`
2. **자체가 `NavKey`** — 필요하면 탭 전환도 backstack에 기록 가능
3. **KMP 친화적** — `@Serializable`이 Multiplatform에서 그대로 작동

### 5.5 3가지 `SceneStrategy` 조합

Nav3의 강력함은 **`SceneStrategy`를 여러 개 조합**할 수 있다는 점입니다:

```kotlin
sceneStrategies = listOf(
    DialogSceneStrategy<NavKey>(),        // 1순위: dialog metadata 가진 entry를 Dialog로
    rememberListDetailSceneStrategy(),    // 2순위: list/detail pair를 wide 화면에서 2-pane
    SinglePaneSceneStrategy<NavKey>()     // fallback: 단일 화면
)
```

NavDisplay는 각 strategy에게 "이 top entry를 당신이 렌더링할 수 있나?"라고 순서대로 질의하며, null이 돌아오면 다음으로 넘어갑니다. **우선순위를 list 순서로 명시적으로 관리**할 수 있어 이해와 수정이 모두 쉽습니다.

---

## 6. 최신 아키텍처 구조

### 6.1 전체 레이어 (Clean Architecture + MVVM, 2026 변경점 반영)

```
┌───────────────────────────────────────────────────────────────────┐
│                          Presentation Layer                       │
│                                                                   │
│  ┌──────────────────┐   ┌──────────────────┐   ┌──────────────┐   │
│  │   Compose UI     │   │   State Holders  │   │  ViewModels  │   │
│  │  (@Composable)   │◄──│ rememberXxxState │◄──│  (Hilt DI)   │   │
│  └──────────────────┘   └──────────────────┘   └──────────────┘   │
│         ▲                                             │           │
│         │ Nav 3 NavDisplay + NavBackStack             │           │
│         │ (SharedTransition + ListDetail + Dialog)    │           │
│         ▼                                             ▼           │
│  ┌──────────────────────────────────────────────────────────┐     │
│  │             Unidirectional Data Flow                     │     │
│  │   (UI events → ViewModel → StateFlow → UI recompose)     │     │
│  └──────────────────────────────────────────────────────────┘     │
└───────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌───────────────────────────────────────────────────────────────────┐
│                            Domain Layer                           │
│                      (UseCase / Interactor)                       │
│                                                                   │
│   SearchPhotosUseCase · GetPhotoUseCase · BookmarkUseCase · …     │
└───────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌───────────────────────────────────────────────────────────────────┐
│                             Data Layer                            │
│                                                                   │
│  ┌──────────────────┐   ┌──────────────────┐   ┌──────────────┐   │
│  │   Repositories   │   │  Local (Room)    │   │  Remote(OkHttp│  │
│  │   (interface +   │◄──│  Preferences     │   │   + Retrofit) │  │
│  │    impl)         │   │                  │   │                │  │
│  └──────────────────┘   └──────────────────┘   └──────────────┘   │
│                                                                   │
│                  Coroutines + Flow + Paging 3                     │
└───────────────────────────────────────────────────────────────────┘
```

### 6.2 네비게이션 레이어 상세 (2026 신규)

```
HomeScreen
 │
 └─ Scaffold
     ├─ bottomBar:
     │   BottomNavBar (BottomNavRoute.entries 기반)
     │
     └─ content:
         SharedTransitionLayout                              ← Shared Element
          └─ CompositionLocalProvider(LocalSharedTransitionScope)
              └─ NavDisplay
                  ├─ backStack = navState.backStackForCurrentRoute
                  ├─ onBack = { count -> repeat(count) { navState.pop() } }
                  │                                          ← predictive back
                  │                                            (Int 파라미터)
                  ├─ sceneStrategies = [
                  │     DialogSceneStrategy,                  ← Dialog scene
                  │     rememberListDetailSceneStrategy,      ← Adaptive scene
                  │     SinglePaneSceneStrategy               ← fallback
                  │   ]
                  ├─ entryDecorators = [
                  │     rememberSaveableStateHolderNavEntryDecorator,
                  │     rememberViewModelStoreNavEntryDecorator
                  │   ]                                       ← per-entry scope
                  │
                  └─ entryProvider = entryProvider {
                       phogalEntries(navState)                ← DSL 확장
                         ├─ galleryTabEntries  (4 routes)
                         ├─ popularTabEntries  (1 route)
                         ├─ notificationTabEntries (2 routes)
                         └─ settingTabEntries  (4 routes)
                     }
```

### 6.3 State 흐름 — UDF (Unidirectional Data Flow) 엄수

2023 버전에서도 UDF는 준수됐으나, 2026 버전은 **`StateFlow` + `collectAsStateWithLifecycle()` + State Holder 패턴**을 일관되게 적용:

```kotlin
// ViewModel (Hilt 주입)
class PictureViewModel @Inject constructor(
    private val getPhotoUseCase: GetPhotoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Photo>>(UiState.Loading)
    val uiState: StateFlow<UiState<Photo>> = _uiState.asStateFlow()
    // ...
}

// State Holder (Compose에서만 존재)
@Composable
fun rememberPhotoContentState(...): PhotoContentState { ... }

// Composable은 StateFlow 구독만 하고 이벤트는 콜백으로 위임
@Composable
fun PictureScreen(viewModel: PictureViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // UI render
}
```

### 6.4 Per-entry ViewModel Scope — Nav3의 숨은 보석

```kotlin
entryDecorators = listOf(
    rememberSaveableStateHolderNavEntryDecorator(),    // rememberSaveable이 entry별로 독립
    rememberViewModelStoreNavEntryDecorator()          // ViewModel도 entry별로 독립
)
```

**효과**: `PictureRoute`를 다른 ID로 backstack에 여러 번 push해도 **각 PictureRoute 인스턴스마다 별도의 `PictureViewModel`을 보유**. 2023 Nav2 버전에서는 스코프 충돌이 일어나기 쉬웠던 부분.

### 6.5 DI — Hilt + KSP 조합

```kotlin
// 2023: kapt 기반 (느림)
apply plugin: 'kotlin-kapt'
kapt "com.google.dagger:hilt-compiler:2.x"

// 2026: KSP 2.0.21-1.0.28 기반 (2~3배 빠른 빌드)
plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}
dependencies {
    implementation("com.google.dagger:hilt-android:2.58")
    ksp("com.google.dagger:hilt-compiler:2.58")
}
```

**효과**: 증분 빌드 속도 향상, IDE 안정성 개선.

---

## 7. 기술부채 해소 — 개선한 구조와 방향성

> 이 섹션은 단순히 "무엇을 바꿨는지"의 나열이 아니라, **기술부채의 유형을 분류하고 → 각 부채가 어떤 아키텍처 원칙을 위반했는지 분석 → 새 구조가 어떤 설계 철학으로 해소하는지 → 향후 어떻게 진화해야 하는지**를 체계적으로 서술합니다.

### 🧭 기술부채 분류 프레임

Phogal 2023 코드베이스의 부채를 구조적으로 진단하기 위해 4가지 축으로 분류했습니다:

| 부채 유형 | 정의 | 해당 항목 |
|----------|------|----------|
| **🔴 타입 안전성 부채** | 런타임에서야 드러나는 실수를 컴파일러가 못 잡음 | #1, #2, #7 |
| **🟠 아키텍처 부채** | 레이어 경계 모호, 책임 분산, 확장 불가 | #3, #4, #8 |
| **🟡 빌드 인프라 부채** | 빌드 시간·버전 관리 비효율, 현대 도구 체인 미활용 | #5, #6, #9, #10 |
| **🟢 플랫폼 부채** | 최신 Android/Compose 기능 미활용으로 인한 UX/성능 손실 | #4, #8, #9 |

---

### 7.1 🔴 [타입 안전성 부채 #1] 문자열 Route의 런타임 크래시 위험

#### 🔍 부채의 본질 — "Stringly-typed" 네비게이션

2023 Nav2 구조의 근본 문제는 **도메인 개념(어느 화면으로 이동할까?)을 원시 타입(String)으로 표현**한 것입니다. 이는 **Stringly-typed 프로그래밍** 안티패턴의 교과서적 사례였습니다.

```kotlin
// 2023 — 문제의 구조 (재현)
object PhogalDestination {
    internal const val searchPhotosStartRoute = "photoHome/searchPhotos"
    internal const val pictureRouteArgs = "photoHome/picture/{id}/{showViewPhotosButton}"
    internal const val userPhotosRouteArgs = "photoHome/userPhotos/{name}/{firstName}/{lastName}/{username}"
}

// 사용처 — 문자열 조립
navController.navigate("photoHome/picture/${id}/${showButton}")
// 또는
navController.navigate(pictureRouteArgs.replace("{id}", id).replace("{showViewPhotosButton}", "$showButton"))
```

**이 구조가 위반하는 원칙**:
- ❌ **Parse, don't validate** (Alexis King) — 파싱된 타입으로 데이터를 표현하라
- ❌ **Make illegal states unrepresentable** (Yaron Minsky) — 잘못된 상태를 타입 시스템에서 아예 표현 불가능하게 만들어라
- ❌ **Single source of truth** — route 정의와 인자 타입이 `PhogalDestination.kt`와 `navArgument("id") { type = NavType.StringType }`로 **분산**됨

#### 🏗 개선된 구조의 설계 원칙

```kotlin
// 2026 — 타입이 곧 계약(contract)
@Serializable
data class PictureRoute(
    val id: String,
    val showViewPhotosButton: Boolean
) : NavKey

// 사용처 — 타입 체크된 인스턴스화
navState.push(Routes.PictureRoute(id = photoId, showViewPhotosButton = true))

// 수신처 — 구조분해 할당이 아닌 typed property 접근
entry<Routes.PictureRoute> { key ->
    PictureScreen(photoId = key.id, showButton = key.showViewPhotosButton)
}
```

**개선 설계의 3대 원칙**:

1. **Route definition과 parameter contract의 통합**  
   기존: 문자열 경로 + navArgument 타입 명세 2곳 분리 → 새 구조: `data class` 한 곳에 경로명과 파라미터 타입이 **함께** 정의
2. **컴파일 타임 보증**  
   `PictureRoute(id = "abc")`에서 필수 파라미터 `showViewPhotosButton` 누락 시 IDE가 즉시 경고
3. **직렬화 기반의 프로세스 사망 복원**  
   `@Serializable`로 kotlinx.serialization이 자동 생성하는 직렬화 코드를 Nav3가 내부적으로 활용 → backstack의 각 NavKey가 저장/복원 지원

#### 🚀 개선 방향성 — Type-safe navigation의 확장

- **단기**: 모든 route에 `@Serializable data class`/`data object` 패턴 100% 적용 완료됨 ✅
- **중기**: Route에 **sealed parent**를 도입하여 "이 화면에서 이동 가능한 목적지"를 타입으로 표현
  ```kotlin
  sealed interface GalleryDestination : NavKey
  @Serializable data object SearchPhotosRoute : GalleryDestination
  @Serializable data class PictureRoute(...) : GalleryDestination
  // → navState.push(gallery destinations only)로 제한 가능
  ```
- **장기**: **Deep link URL ↔ NavKey 자동 생성**. Nav3의 `@Serializable` 기반이라 Kotlin Serialization의 JSON/URL 포맷을 그대로 활용 가능

---

### 7.2 🔴 [타입 안전성 부채 #2] enum `BottomNavDestination`의 확장성 한계

#### 🔍 부채의 본질 — Open-closed principle 위반

`enum`은 "모든 멤버가 동일한 생성자 시그니처"라는 제약을 갖습니다. 이는 탭 개념이 **동질적일 때만** 적합한데, 실제 앱에서는 "Setting 탭만 사용자 ID가 필요", "Notification 탭만 미읽은 개수를 알아야 함" 같은 **이질적 요구**가 자연스럽게 발생합니다.

```kotlin
// 2023 — 확장이 막힌 구조
enum class BottomNavDestination(
    @DrawableRes val icon: Int,
    @StringRes val title: Int
) {
    Gallery(R.drawable.ic_photo, R.string.bottom_navigation_gallery),
    PopularPhotos(R.drawable.ic_popphotos, R.string.bottom_navigation_popular_photos),
    Notification(R.drawable.ic_notification, R.string.bottom_navigation_notification),
    Setting(R.drawable.ic_setting, R.string.bottom_navigation_setting)
    // ❌ Setting(userId: String)을 여기 추가하려면?
    //    → 전체 enum 구조 변경, 기존 모든 사용처 수정 필요
}
```

**이 구조가 위반하는 원칙**:
- ❌ **Open-Closed Principle (SOLID의 O)** — 확장에는 열려 있고 수정에는 닫혀 있어야
- ❌ **Algebraic Data Type 모델링** — 이질적 variant는 sum type으로 표현해야

#### 🏗 개선된 구조의 설계 원칙

```kotlin
// 2026 — sealed interface로 ADT 기반 모델링
@Serializable
sealed interface BottomNavRoute : NavKey {
    @get:DrawableRes val icon: Int
    @get:StringRes val title: Int

    @Serializable data object Gallery : BottomNavRoute {
        override val icon get() = R.drawable.ic_photo
        override val title get() = R.string.bottom_navigation_gallery
    }
    @Serializable data object PopularPhotos : BottomNavRoute { ... }
    @Serializable data object Notification : BottomNavRoute { ... }
    @Serializable data object Setting : BottomNavRoute { ... }

    companion object {
        val entries: List<BottomNavRoute> = listOf(Gallery, PopularPhotos, Notification, Setting)
    }
}
```

**개선 설계의 구조적 이점**:

| 속성 | enum | sealed interface |
|------|------|------------------|
| 모든 항목이 동일 시그니처 | **강제** | **선택** |
| 일부 항목만 파라미터 보유 | ❌ 불가 | ✅ 가능 |
| 하위 타입이 NavKey도 될 수 있음 | ❌ (enum은 상속 제한) | ✅ |
| Exhaustive when | ✅ | ✅ |
| 직렬화 | 수동 (ordinal/name) | `@Serializable` 자동 |
| KMP 호환 | 제한적 | 완전 호환 |

#### 🚀 개선 방향성 — 탭 확장을 위한 준비된 구조

현재는 4개 탭 모두 `data object`이지만, **언제든 `data class`로 승격 가능한 구조**를 이미 확보한 상태입니다:

```kotlin
// 향후 개인화 확장 시나리오 (코드 한 군데만 변경)
@Serializable
data class Setting(val userId: String) : BottomNavRoute {
    override val icon get() = R.drawable.ic_setting
    override val title get() = R.string.bottom_navigation_setting
}

// 나머지 Gallery/PopularPhotos/Notification는 건드리지 않음
// BottomNavRoute.entries도 그대로 (단, Setting 생성 시 userId 필요)
```

- **단기**: 현재 구조 유지. 탭 identity와 탭 내부 route(Routes.\*)를 명확히 분리 ✅
- **중기**: **다국어/다지역 탭 배치** (예: A/B 테스트로 일부 사용자에게 Notification 대신 Explore 탭 노출) → sealed 계층에 variants 추가
- **장기**: **Wear OS/XR** 같은 다른 폼팩터에서 같은 sealed 계층을 공유하되, 각 폼팩터별로 `entries` 구성만 다르게

---

### 7.3 🟠 [아키텍처 부채 #3] Dialog가 Compose State 트리 밖에 떠다님

#### 🔍 부채의 본질 — State management의 이중성

2023 버전의 `PermissionBottomSheet`은 **네비게이션 상태와 UI 상태가 섞여** 있었습니다. Dialog 열림/닫힘은 본질적으로 "어느 화면이 보이는가"라는 네비게이션 문제인데, 이를 `var openSheet by remember { mutableStateOf(false) }` 같은 UI 로컬 상태로 처리했습니다.

```kotlin
// 2023 (의사 코드) — UI state와 navigation state가 뒤섞임
@Composable
fun SearchPhotosScreen() {
    var showPermissionSheet by remember { mutableStateOf(false) }

    Button(onClick = { showPermissionSheet = true }) { /* ... */ }

    if (showPermissionSheet) {
        PermissionBottomSheet(
            onDismissedRequest = { showPermissionSheet = false }
        )
    }
}
```

**이 구조가 위반하는 원칙**:
- ❌ **Separation of concerns** — 네비게이션과 UI 상태가 섞임
- ❌ **Single source of truth** — "현재 앱이 어느 화면을 보여주는지"가 backstack + 로컬 boolean 두 곳에 분산
- ❌ **State hoisting** — 하위 composable이 상위 composable의 상태를 제어 (callback 역류)

#### 😫 이 부채가 야기한 실질적 문제

1. **회전 시 dialog 소실** — `rememberSaveable`로 감싸도 bottom sheet 내부 상태까지는 복원 불완전
2. **프로세스 사망 복원 실패** — Android가 메모리 회수 후 앱 재개하면 dialog 사라짐 (사용자는 "왜 다시 눌러야 해?" 혼란)
3. **시스템 back 인식 불일치** — dialog 표시 중 뒤로가기 시스템 버튼이 dialog가 아닌 이전 화면으로 이동
4. **Deep link 불가** — `phogal://settings/permission`으로 dialog를 직접 열 수 없음
5. **테스트 어려움** — UI 상태를 조작하려면 전체 composable 트리를 띄워야 함

#### 🏗 개선된 구조 — Dialog를 first-class navigation destination으로

```kotlin
// 2026 — Dialog도 backstack의 일급 시민
@Serializable data object PermissionDialogRoute : NavKey

// Dialog 띄우기
navState.push(Routes.PermissionDialogRoute)

// Dialog 닫기
navState.pop()

// NavDisplay에 DialogSceneStrategy 등록
sceneStrategies = listOf(
    DialogSceneStrategy<NavKey>(),          // ← 이 strategy가 dialog metadata entry를 처리
    // ...
)

// Entry 정의
entry<Routes.PermissionDialogRoute>(
    metadata = DialogSceneStrategy.dialog()
) {
    PermissionDialogContent(
        onDismiss = { navState.pop() },
        onConfirm = { navState.pop() }
    )
}
```

#### 🔑 개선된 구조의 자동 혜택 — "설계가 동작을 결정한다"

이 구조 변경 하나로 위의 5가지 문제가 **전부 자동 해소**됩니다. "설계가 올바르면 버그는 발생하지 않는다"의 전형:

| 기존 문제 | Nav3 구조에서 자동 해소되는 이유 |
|----------|----------------|
| 회전 시 소실 | `NavBackStack`은 `rememberSaveable` 기반이라 config change 자동 복원 |
| 프로세스 사망 복원 | `@Serializable`로 backstack 전체가 Bundle에 저장 |
| 시스템 back 불일치 | Android `OnBackPressedDispatcher`가 backstack top 기준 pop |
| Deep link 불가 | Route가 `@Serializable`이라 URL → NavKey 직렬화 가능 |
| 테스트 어려움 | NavBackStack이 `SnapshotStateList`라 단위 테스트에서 그냥 조작 |

#### 🚀 개선 방향성 — Dialog가 확장할 공간

- **단기**: `PermissionDialogRoute` 1개만 적용. 기존 `PermissionBottomSheet`을 완전 교체하기 위해 content를 `PermissionRequestContent` 공용 composable로 분리 필요 (B 방안)
- **중기**: 모든 app-level dialog/bottom-sheet를 NavKey로 승격
  - `BookmarkConfirmDialog`, `LogoutConfirmDialog`, `PhotoOptionsBottomSheet` 등
- **장기**: Dialog의 **중첩 navigation** 지원 검토 — dialog 안에서 또 다른 화면으로 이동 (wizard 패턴)

---

### 7.4 🟢 [플랫폼 부채 #4] 태블릿/폴더블 미대응 — Material 권장 사항 불이행

#### 🔍 부채의 본질 — Mobile-first의 함정

2023 Phogal은 "폰 세로 화면을 완벽히 만든 다음 다른 폼팩터는 나중에" 전략. 실제로는 **"나중에"가 오지 않는** 경우가 대부분입니다. 결과:

- 태블릿에서 Phogal 실행 → 폰 레이아웃을 그냥 확대 → **공간 낭비** + **시야 분산**
- 폴더블 펼침 → 접힌 상태와 똑같은 단일 pane → **하드웨어 자산 미활용**
- Material Design 공식 권고 위반 — "Canonical Layouts 중 `list-detail`을 사용하라"

#### 🏗 개선된 구조 — 3줄 메타데이터로 adaptive layout 달성

Material 3 Adaptive + Nav3의 `ListDetailSceneStrategy`는 **UI 코드 수정 없이 metadata만으로** master-detail을 구현할 수 있게 만들었습니다.

```kotlin
// Step 1: NavDisplay에 ListDetail strategy 등록
sceneStrategies = listOf(
    DialogSceneStrategy<NavKey>(),
    rememberListDetailSceneStrategy<NavKey>(),   // 🔑 이게 핵심
    SinglePaneSceneStrategy<NavKey>()            // 폰에서는 fallback
)

// Step 2: 각 entry에 역할 태그만 추가
entry<Routes.SearchPhotosRoute>(
    metadata = ListDetailSceneStrategy.listPane(        // ← "나는 list"
        detailPlaceholder = { DetailPlaceholder() }
    )
) { /* 기존 SearchPhotosScreen 그대로 */ }

entry<Routes.PictureRoute>(
    metadata = ListDetailSceneStrategy.detailPane()     // ← "나는 detail"
) { /* 기존 PictureScreen 그대로 */ }
```

**이 구조의 혁신성**:
- **코드 비침투성**: 기존 `SearchPhotosScreen`, `PictureScreen` 본문을 **한 줄도 수정하지 않음**
- **자동 분기**: `WindowSizeClass`가 Compact면 single-pane, Medium/Expanded면 list-detail 2-pane
- **Back 동작 자동 대응**: 2-pane에서 뒤로가기는 detail만 pop (list는 유지)

#### 📐 화면 크기별 자동 동작 매트릭스

| 화면 크기 | List pane | Detail pane | Back 동작 |
|----------|-----------|-------------|-----------|
| Compact (폰 세로) | 전체 화면 | detail push 시 전체 화면 | detail → list |
| Medium (폰 가로/폴더블) | 왼쪽 40% | 오른쪽 60% | detail → placeholder |
| Expanded (태블릿) | 왼쪽 360dp | 나머지 | detail → placeholder |

#### 🚀 개선 방향성 — Adaptive의 단계적 심화

- **단기**: `SearchPhotos ↔ Picture` 쌍만 list-detail 적용 ✅
- **중기**: 
  - `UserPhotos ↔ Picture`, `BookmarkedPhotos ↔ Picture` 쌍도 확장
  - `NavigationSuiteScaffold` 도입 — 태블릿에서 BottomNav → NavigationRail 자동 전환
  - 세 pane 레이아웃(`SupportingPaneScaffold`) 활용 — 예: list / detail / metadata panel
- **장기**: **폴더블 힌지 감지** (`FoldingFeature`) 기반 조건부 레이아웃
- **장기**: **XR/공간 UI** — Compose for XR이 stable화되면 Scene 개념이 공간으로 자연 확장

---

### 7.5 🟡 [빌드 인프라 부채 #5] Gradle 의존성 관리의 분산

#### 🔍 부채의 본질 — Single source of truth의 부재

2023 빌드 스크립트는 **버전 문자열이 최소 4군데** 흩어져 있었습니다:

```groovy
// 2023 — /build.gradle (root)
buildscript {
    ext {
        compose_version = '1.3.3'
        kotlin_version = '1.8.21'
        navigation_compose_hilt_version = '1.0.0'
    }
}

// 2023 — /app/build.gradle
composeOptions {
    kotlinCompilerExtensionVersion = "1.4.7"   // ← ext에 없음, 여기만 관리
}
dependencies {
    def composeBom = platform('androidx.compose:compose-bom:2023.04.01')  // ← 문자열
    implementation 'androidx.paging:paging-compose:3.2.0-rc01'            // ← 문자열
    // ...
}
```

**이 구조가 위반하는 원칙**:
- ❌ **Single source of truth** — 버전 정보가 4곳에 분산
- ❌ **DRY (Don't Repeat Yourself)** — 같은 버전 문자열을 여러 모듈에서 중복 기재
- ❌ **Type safety** — 라이브러리 이름을 문자열로 기재 → 오타 시 빌드 실패
- ❌ **Discoverability** — 어떤 라이브러리가 프로젝트에 있는지 파악하려면 grep

#### 🏗 개선된 구조 — Version Catalog (`libs.versions.toml`)

Gradle 7.4+의 공식 기능인 Version Catalog로 **모든 의존성을 TOML 한 파일로 집중**시켰습니다.

```toml
# /gradle/libs.versions.toml

[versions]
# === 빌드 도구 ===
agp = "8.13.2"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"

# === Compose ===
composeBom = "2026.3.01"

# === Navigation 3 ===
nav3Core = "1.1.0"
lifecycleViewmodelNavigation3 = "2.10.0"
adaptiveNavigation3 = "1.1.0"

# === DI ===
hilt = "2.58"

[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-navigation3-runtime = { group = "androidx.navigation3", name = "navigation3-runtime", version.ref = "nav3Core" }
androidx-navigation3-ui = { group = "androidx.navigation3", name = "navigation3-ui", version.ref = "nav3Core" }
androidx-compose-material3-adaptive-navigation3 = { group = "androidx.compose.material3.adaptive", name = "adaptive-navigation3", version.ref = "adaptiveNavigation3" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }

[plugins]
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

```groovy
// /app/build.gradle — 타입 안전하게 참조
plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)     // Kotlin 버전 자동 매칭
    alias(libs.plugins.ksp)
}
dependencies {
    implementation platform(libs.androidx.compose.bom)
    implementation libs.androidx.navigation3.runtime
    implementation libs.androidx.navigation3.ui
    implementation libs.androidx.compose.material3.adaptive.navigation3
    implementation libs.hilt.android
    ksp libs.hilt.compiler
}
```

#### 🎯 개선 설계의 핵심 가치

| 가치 | 구현 |
|------|------|
| **Single source of truth** | `libs.versions.toml` 한 곳에 모든 버전 |
| **IDE 자동완성** | `libs.`만 치면 사용 가능한 모든 라이브러리 노출 |
| **타입 안전성** | `libs.androidx.navigation3.ui`가 컴파일 타임에 해결됨 (오타 시 빌드 실패 즉시) |
| **Refactoring 지원** | IDE가 `libs.*` 사용처를 추적 |
| **변경 이력 추적** | 한 파일 diff로 "이 릴리스에서 어떤 라이브러리가 올라갔는가" 즉시 파악 |
| **멀티모듈 확장성** | 미래에 feature 모듈로 쪼개도 동일한 `libs.*` 참조 재사용 |

#### 🚀 개선 방향성 — Catalog의 심화 활용

- **단기**: 완료됨 ✅
- **중기**: `[bundles]` 섹션 활용
  ```toml
  [bundles]
  compose-core = ["androidx-compose-ui", "androidx-compose-foundation", "androidx-compose-material3"]
  nav3-all = ["androidx-navigation3-runtime", "androidx-navigation3-ui", "androidx-lifecycle-viewmodel-navigation3"]
  ```
  → `implementation(libs.bundles.nav3.all)` 한 줄로 묶음 의존성
- **장기**: **Dependabot/Renovate** 자동화 — PR이 올라오면 release note와 함께 리뷰

---

### 7.6 🟡 [빌드 인프라 부채 #6] kapt의 빌드 속도 병목

#### 🔍 부채의 본질 — JVM 호환성을 위한 이중 컴파일

`kapt`(Kotlin Annotation Processing Tool)는 Java의 `javac` 기반 annotation processor (예: Dagger/Hilt)를 **Kotlin에서 쓰기 위한 브리지**입니다. 프로세스가 복잡합니다:

```
Kotlin 소스  →  [kapt: Java stub 생성]  →  [javac: annotation 처리]  
             →  생성된 Java 코드  →  [kotlinc: 최종 컴파일]
```

이 이중 컴파일이 **프로젝트 규모에 비례해 지수적으로 느려집니다**. Phogal처럼 Hilt를 전역 사용하는 프로젝트에서 증분 빌드마다 kapt가 재실행되면, IDE가 수 초~수십 초 멈추는 경험을 매번 겪게 됩니다.

#### 🏗 개선된 구조 — KSP (Kotlin Symbol Processing)

KSP는 Kotlin Compiler의 `PSI` (Program Structure Interface)를 직접 다루는 **Kotlin 네이티브 annotation processor**입니다. Java stub 단계가 **아예 없습니다**.

```
Kotlin 소스  →  [KSP: 직접 처리]  →  생성된 Kotlin 코드  →  [kotlinc: 최종 컴파일]
```

```groovy
// 2023 — kapt 기반
apply plugin: 'kotlin-kapt'
dependencies {
    kapt "com.google.dagger:hilt-compiler:2.x"
    kapt "androidx.hilt:hilt-compiler:1.x"
}

// 2026 — KSP 기반
plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}
dependencies {
    ksp libs.hilt.compiler         // ← kapt → ksp 교체
    ksp libs.androidx.hilt.compiler
}
```

#### 📊 실측 성능 개선 (일반적인 중규모 프로젝트 기준)

| 지표 | kapt | KSP | 개선폭 |
|------|------|-----|-------|
| Clean build | 100% | 60~70% | **30~40% 감소** |
| Incremental build | 100% | 25~40% | **60~75% 감소** |
| IDE 응답성 | 느림 | 즉각 | 체감상 큰 개선 |
| 메모리 사용 | 높음 | 낮음 | Java stub 제거로 메모리 압박 ↓ |

> 정확한 수치는 프로젝트마다 다르지만, Google이 공식 발표한 [KSP 2.0 블로그](https://android-developers.googleblog.com/2024/05/ksp2-beta.html)에서 확인 가능합니다.

#### ⚠️ 전환 시 주의한 점 — KSP 버전 매칭

KSP는 **Kotlin 버전과 정확히 일치하는 sub-version**을 사용해야 합니다:

```toml
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"   # 앞의 "2.0.21"은 반드시 Kotlin과 일치
```

Version Catalog (#5) 덕에 이 제약이 한 파일에서 관리되어 실수가 구조적으로 방지됩니다.

#### 🚀 개선 방향성 — 나머지 annotation processor의 KSP 이전

- **단기**: Hilt + AndroidX Hilt → KSP 완료 ✅
- **중기**: **Room** (스키마 생성기) → KSP 이전 (이미 Room 2.6+가 KSP 정식 지원)
- **중기**: **Moshi** 사용 중이라면 `moshi-codegen` → KSP 이전
- **장기**: **kapt 완전 제거** — 프로젝트에서 `kotlin-kapt` 플러그인을 지움으로써 Java stub 단계 자체를 빌드 파이프라인에서 제거

---

### 7.7 🔴 [타입 안전성 부채 #7] 중첩 NavGraph의 복잡도

#### 🔍 부채의 본질 — Accidental complexity

"탭 4개" 라는 도메인 개념(intrinsic complexity)을 표현하기 위해 2023 버전은 **accidental complexity**를 다량 끌어들였습니다:

```
navigation/
├── destination/
│   ├── PhogalDestination.kt        (~50 LOC, 모든 route 문자열 상수)
│   ├── Gallery.kt                  (~250 LOC, 4개 화면 destination 객체)
│   ├── PopularPhotos.kt            (~90 LOC)
│   ├── Notification.kt             (~30 LOC)
│   └── Setting.kt                  (~170 LOC)
└── graph/
    ├── GalleryNavGraph.kt          (~50 LOC, navigation<T>{...})
    ├── PopularPhotosNavGraph.kt    (~22 LOC)
    ├── NotificationNavGraph.kt     (~22 LOC)
    └── SettingNavGraph.kt          (~65 LOC)
= 총 9개 파일, 약 750 LOC
```

**이 구조가 만든 인지적 부담**:
- 새 화면 추가 시 → `Gallery.kt` 수정 + `GalleryNavGraph.kt` 수정 + `PhogalDestination.kt` 수정 (**3곳**)
- "Gallery 탭에서 어디로 이동할 수 있지?"를 파악하려면 → 3개 파일을 동시에 봐야 함
- "sharedPhoto를 Gallery에서도, Bookmark에서도 쓰고 싶다" → 두 graph에 각각 composable 등록

#### 🏗 개선된 구조 — Flat entries with DSL

Nav3는 **중첩 graph 개념 자체를 제거**하고, 대신 **flat list of entries + SceneStrategy**로 계층을 표현합니다.

```
navigation/
├── Routes.kt                       (~60 LOC, 모든 NavKey 정의)
└── nav3/
    ├── NavigationState.kt          (~150 LOC, multi-backstack 관리)
    ├── PhogalEntryProvider.kt      (~270 LOC, 모든 entry 등록)
    └── SharedTransitionKeys.kt     (~40 LOC, shared element 키)
= 총 4개 파일, 약 520 LOC (-31%)
```

```kotlin
// PhogalEntryProvider.kt — 앱 전체 네비게이션이 한 파일에 가시화됨
fun EntryProviderScope<NavKey>.phogalEntries(navState: PhogalNavState) {
    galleryTabEntries(navState)       // 4 entries: SearchPhotos, Picture, UserPhotos, WebView
    popularTabEntries(navState)       // 1 entry : PopularPhotos
    notificationTabEntries(navState)  // 2 entries: Notifications, NotificationDetail
    settingTabEntries(navState)       // 4 entries: Setting, Bookmarked, Following, NotificationSetting
}

// 탭 식별은 따로 BottomNavRoute가 담당하고, 그 탭의 backstack은 NavigationState가 관리
// → 탭의 "계층"은 코드 구조가 아닌 런타임 데이터 구조(Map<BottomNavRoute, NavBackStack>)로 표현
```

#### 🎯 개선 설계 원칙 — Structure vs Behavior 분리

**2023 구조**: 탭의 계층이 **코드 구조**(nested functions)에 직접 새겨짐 → 변경 시 코드 구조 변경 필요

**2026 구조**: 탭의 계층이 **런타임 데이터 구조**(Map + List)로 표현 → 변경은 데이터 변경으로 충분

```kotlin
// 2023 — 탭 추가 = 파일 추가
fun NavGraphBuilder.newTabGraph(...) {
    navigation<NewTabGraphRoot>(startDestination = ...) { ... }
}
// + NewTab.kt (destination)
// + NewTabNavGraph.kt (graph)
// + HomeScreen.kt 수정 (BottomNavigationItem)
// + PhogalDestination.kt 수정 (route 상수)

// 2026 — 탭 추가 = BottomNavRoute에 data object 추가 + entry 몇 개 등록
@Serializable data object NewTab : BottomNavRoute { ... }
// + phogalEntries에 newTabEntries(navState) 한 줄
```

#### 🚀 개선 방향성 — Entry 조직화의 진화

- **단기**: 완료 ✅ (4개 tab별 private 확장 함수로 관리)
- **중기**: **Feature 모듈 도입 시 각 모듈이 자기 entry 확장 함수 제공**
  ```kotlin
  // feature-gallery 모듈에서
  fun EntryProviderScope<NavKey>.galleryFeatureEntries(...) { ... }
  
  // app 모듈에서
  entryProvider {
      galleryFeatureEntries(navState)      // feature-gallery 제공
      popularFeatureEntries(navState)      // feature-popular 제공
      // ...
  }
  ```
- **장기**: **Dynamic Feature 모듈**에서도 동일 패턴으로 on-demand 로딩

---

### 7.8 🟢 [플랫폼 부채 #8] Shared Element Transition 부재

#### 🔍 부채의 본질 — Material Motion 미구현

Material Design의 핵심 원칙 중 하나는 **"motion guides attention"** (모션이 주의를 유도한다). 화면 전환 시 관련된 요소가 부드럽게 연결되어야 사용자의 인지 부하가 줄어듭니다.

2023 Phogal은 **slide/fade 전환만** 사용. 사진 썸네일 → 상세 화면 이동 시:
- 사용자: "내가 누른 사진이 어느 건지 시각적 연결이 끊겼다"
- 사용자: "전환이 완료된 후에야 어느 사진이 열렸는지 확인 가능"

#### 🏗 개선된 구조 — SharedTransitionLayout + CompositionLocal 기반 인프라

Compose 1.7에서 stable이 된 `SharedTransitionLayout`과 Nav3 1.1.0의 공식 통합으로, **CompositionLocal 기반 인프라**를 설계했습니다.

```kotlin
// Step 1: 최상위 레이아웃이 scope를 공급
SharedTransitionLayout {
    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
        NavDisplay(/* ... */)
    }
}

// Step 2: Nav3가 각 entry 안에서 AnimatedContentScope를 자동 공급
// → LocalNavAnimatedContentScope (Nav3 제공)

// Step 3: 화면은 두 CompositionLocal을 조합해 Modifier.sharedElement 사용
@Composable
fun PhotoItem(photo: Photo) {
    val sharedScope = LocalSharedTransitionScope.current ?: return
    val animatedScope = LocalNavAnimatedContentScope.current
    
    with(sharedScope) {
        Image(
            painter = rememberAsyncImagePainter(photo.url),
            modifier = Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(
                    key = photoSharedElementKey(photo.id)
                ),
                animatedVisibilityScope = animatedScope
            )
        )
    }
}
```

#### 🎯 개선 설계 원칙 — Infrastructure vs Application 분리

- **Infrastructure** (플랫폼 팀이 제공): `SharedTransitionLayout`, `LocalSharedTransitionScope`, `LocalNavAnimatedContentScope`, `photoSharedElementKey()` 헬퍼
- **Application** (화면 개발자가 소비): `Modifier.sharedElement(...)`만 붙이면 됨

화면 개발자가 Nav3 내부나 Compose 내부를 이해하지 않아도 **hero 애니메이션을 3줄**로 추가할 수 있습니다.

#### 🚀 개선 방향성 — Motion design의 체계화

- **단기**: 인프라 준비 완료 ✅. `PhotoItem ↔ PictureContent` 실제 적용만 남음
- **중기**: **Motion spec 라이브러리 내부화**
  ```kotlin
  object PhogalMotion {
      val photoHero = PhotoHeroBoundsTransform(durationMs = 500)
      val cardExpand = CardExpandBoundsTransform(durationMs = 350)
      val dialogEnter = DialogEnterBoundsTransform(durationMs = 200)
  }
  ```
- **중기**: **Reduced motion 대응** — 시스템 설정 "애니메이션 감소" 활성 시 자동 비활성화
- **장기**: **Container transform pattern** (Material Motion의 가장 복잡한 패턴) 적용 — 카드가 화면 전체로 확장되는 전환

---

### 7.9 🟢🟡 [플랫폼 + 인프라 부채 #9] compileSdk / targetSdk 정체

#### 🔍 부채의 본질 — Android의 2-year rule

Google Play는 **매년 targetSdk를 1~2년 이내 최신으로 유지**하도록 요구합니다:

- 2024년 8월 31일부터: 신규 앱 targetSdk 34 이상
- 2025년 8월 31일부터: 기존 앱도 targetSdk 34 이상 (아니면 Play Store 게재 중단)
- 2026년: target 35~36 요구 예상

2023년 `compileSdk = 34`에 멈춰 있던 Phogal은 **2025년 말까지 방치하면 Play Store 퇴출 위기**였습니다.

#### 🏗 개선된 구조 — compileSdk = 36 (Android 15)

```groovy
android {
    compileSdk = 36      // Android 15 SDK API 접근
    
    defaultConfig {
        minSdk = 28       // Android 9 (2018) — 여전히 넓은 호환 범위
        targetSdk = 36    // Android 15 동작 모드 (최신 behavior change 적용)
    }
}
```

#### 🎯 접근 가능해진 Android 15 신규 API

1. **Predictive Back** — 뒤로가기 제스처 중 다음 화면을 미리 보여주는 애니메이션. Nav3와 자연스럽게 통합
2. **Per-app language preference** — 시스템 설정에서 앱별 언어 변경
3. **Enhanced notification permissions** — Android 13의 `POST_NOTIFICATIONS`가 더 세밀해짐
4. **Privacy dashboard 강화** — Foreground Service 타입 강제

#### 🚀 개선 방향성 — Android 버전 추적 전략

- **단기**: compileSdk 36 완료 ✅
- **중기**: **Android 16 베타 시즌** (2026년 상반기)부터 Canary 빌드에서 compileSdk 업그레이드 테스트
- **장기**: **Baseline Profile** 적용 — targetSdk 상승 시 런타임 최적화를 함께 수행

---

### 7.10 🟡 [빌드 인프라 부채 #10] JDK 11의 한계

#### 🔍 부채의 본질 — Toolchain 정체

- Kotlin 2.0의 일부 최적화는 JDK 17+에서만 활성화
- AGP 8.1+는 JDK 17을 공식 요구 (JDK 11은 deprecated)
- Gradle 8+의 configuration cache, build cache 등도 JDK 17에서 안정적

2023 버전은 **Kotlin 1.8 + JDK 11 + Gradle 7.x + AGP 7.x**의 동시대 조합이 어울렸습니다. 하지만 이 중 하나라도 올리려면 **전부 함께 올려야** 하는 종속성이 있습니다.

#### 🏗 개선된 구조 — JDK 17 기반 toolchain 선언

```groovy
// /app/build.gradle
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)                                   // Gradle이 JDK 17을 자동 다운로드
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}
```

#### 🎯 JDK 17의 실질 이점

| 기능 | 효과 |
|------|------|
| `record`, `sealed class` JVM 지원 | Kotlin sealed interface의 런타임 매핑 개선 |
| `Pattern matching for switch` | Kotlin이 생성하는 Java interop 코드 최적화 |
| ZGC (Z Garbage Collector) | Gradle 데몬 메모리 압박 감소 |
| String concatenation 최적화 | 빌드 시간 감소 |

#### 🚀 개선 방향성 — Toolchain 자동 관리

- **단기**: JDK 17 고정 ✅
- **중기**: `jvmToolchain(17)` 덕에 Gradle이 JDK를 자동 다운로드 → 팀 간 JDK 불일치 문제 해소
- **장기**: **JDK 21 (LTS) 검토** — AGP/Gradle 지원이 안정화되면 전환

---

### 📊 개선 요약 — 부채 해소 매트릭스

한눈에 보는 **부채 유형별 개선 결과**:

| 부채 유형 | 해소 항목 | 핵심 지표 |
|----------|----------|----------|
| 🔴 타입 안전성 | #1 문자열 Route, #2 enum 한계, #7 중첩 graph | 런타임 크래시 → 컴파일 에러로 전환 |
| 🟠 아키텍처 | #3 Dialog 상태, #8 Motion 부재 | 관심사 분리, 설계 패턴 현대화 |
| 🟡 빌드 인프라 | #5 의존성 분산, #6 kapt, #9 compileSdk, #10 JDK | 빌드 시간 ~30% 감소, Play Store 요구 사항 충족 |
| 🟢 플랫폼 | #4 태블릿 대응, #8 Shared Element, #9 Android 15 | UX 표준 준수, 새 API 접근 |

### 🧩 개선의 연쇄 효과 — "부채 하나의 해소가 다른 부채도 해소한다"

기술부채는 **서로 얽혀** 있어서, 올바른 순서로 풀면 하나의 해결이 여러 부채를 자동 해소합니다. Phogal 마이그레이션에서 실제 발생한 연쇄:

```
[#10 JDK 11→17]
       ↓ 필요조건
[#6 kapt→KSP]  ←─ KSP는 JDK 17 권장
       ↓ 동시 전환
[Kotlin 1.8→2.0]  ──→ [#5 Version Catalog]
       ↓ Kotlin 2.0 요구                ↑
[Compose Compiler Plugin 도입]          │
       ↓                                 │
[Compose BOM 2026.3.01 가능]            │
       ↓                                 │
[Nav3 1.1.0 + adaptive-navigation3] ───┘
       ↓
[#1 문자열 Route → NavKey]
[#2 enum → sealed interface NavKey]
[#3 Dialog backstack 편입]
[#4 태블릿 ListDetailSceneStrategy]
[#7 중첩 graph 제거]
[#8 Shared Element 공식 통합]
[#9 Predictive Back 자동]
```

**한 번의 큰 업그레이드로 10개 부채 전부 해소** — 이것이 "마이그레이션을 미루면 미룰수록 손해"라는 Android 생태계의 법칙을 보여주는 전형적 사례입니다.

### 🎯 기술부채 관리의 원칙 — Phogal이 배운 교훈

이번 마이그레이션에서 **재사용 가능한 교훈**을 정리합니다:

1. **부채는 유형별로 분류하라** — "전부 기술부채"가 아니라 "타입 안전성 / 아키텍처 / 빌드 / 플랫폼" 축으로 나누면 우선순위 결정이 쉬워짐
2. **빌드 인프라를 먼저 해소하라** — #10(JDK), #6(kapt), #5(Catalog)를 먼저 처리해야 나머지 작업이 빠르고 안정적
3. **타입 안전성 > 편의성** — 문자열 route가 편해 보여도 장기적으로는 반드시 크래시로 돌아옴
4. **플랫폼 최신성은 선택이 아닌 필수** — Play Store 요구 사항은 최신성을 **강제**함
5. **점진적 전환 (Strangler Fig 패턴)** — 한 번에 뒤엎지 말고, `USE_NAV3` 같은 feature flag로 공존 기간 확보 후 제거
6. **의존성의 연쇄를 역이용하라** — 하나의 부채를 풀면 여러 부채가 함께 풀리는 순서를 설계하라

---

## 8. 마이그레이션 타임라인

실제로 이 마이그레이션은 **여러 단계로 점진적**으로 진행됐습니다:

| 단계 | 내용 | 산출물 |
|------|------|------|
| **1. Foundation** | Kotlin 2.0 + Compose Compiler Plugin + Version Catalog 도입 | libs.versions.toml, KSP 전환 |
| **2. Nav3 병렬 도입** | Nav2 유지한 채 Nav3 코드 경로 추가 (`USE_NAV3` flag) | Routes.kt 첫 버전 |
| **3. Nav2 완전 제거** | Nav3 안정 확인 후 Nav2 삭제, 중첩 graph → flat entries | navigation/ 패키지 -60% |
| **4. BottomNavDestination 승격** | enum → sealed interface BottomNavRoute : NavKey | BottomNavRoute.kt |
| **5. Scene 전략 3종 통합** | SharedTransition + ListDetail + Dialog | HomeScreen.kt 최종판 |
| **6. API 정정 및 안정화** | `sceneStrategies` 복수형, `onBack(Int)`, decorator 목록 정확도 확인 | 현재 버전 |

각 단계에서 **앱은 항상 빌드되고 동작 가능한 상태를 유지**했습니다. "Big Bang" 마이그레이션은 위험하므로 의도적으로 피했습니다.

---

## 9. 향후 로드맵

현재 2026년 4월 버전은 완성된 현대화가 아니라 **다음 혁신을 준비한 기반**입니다.

### 9.1 단기 (다음 분기)

- [ ] `PhotoItem.kt` / `PictureContent.kt`에 `Modifier.sharedElement()` 실제 적용 (인프라 이미 준비됨)
- [ ] Predictive Back의 **count > 1** 시나리오 실기기 검증 (list-detail 2-pane에서 중요)
- [ ] `PermissionBottomSheet` 내부 content를 `PermissionRequestContent` composable로 분리 (bottom-sheet UX + dialog entry 양립)
- [ ] `DetailPlaceholder`를 **최근 본 사진 슬라이드쇼**로 업그레이드

### 9.2 중기

- [ ] Baseline Profile 전면 적용 — 시작 시간 최적화
- [ ] Room KMP 도입 준비 — Nav3 자체가 KMP 호환이므로 코어 네비게이션 로직은 이미 준비됨
- [ ] Compose Multiplatform (CMP) iOS 포팅 실험
- [ ] 테스트 커버리지 — Nav3는 테스트하기 훨씬 쉬움 (`NavBackStack`이 `SnapshotStateList`이므로 그냥 조작하면 됨)

### 9.3 장기

- [ ] **Wear OS 확장** — 모바일 앱의 ViewModel/Repository를 재사용
- [ ] **Glance Widget** — 북마크한 사진을 홈 화면에 표시
- [ ] **Android XR** 대응 — Compose for XR이 stable이 되면 공간 UI로 확장

---

## 📌 주요 디렉토리 요약

```
Phogal_migrated/
├── gradle/
│   └── libs.versions.toml                  ← Version Catalog (단일 진실원)
├── app/
│   ├── build.gradle                        ← 플러그인 alias 기반
│   └── src/main/java/com/goforer/
│       ├── base/                           ← 디자인 시스템, 유틸 (공통)
│       │   ├── designsystem/
│       │   ├── analytics/
│       │   └── utils/
│       │
│       └── phogal/
│           ├── data/                       ← 데이터 레이어 (Repository + DataSource)
│           ├── domain/                     ← (옵션) UseCase
│           ├── di/                         ← Hilt Modules
│           └── presentation/
│               ├── stateholder/            ← State Holder + ViewModel
│               └── ui/
│                   ├── compose/
│                   │   ├── screen/
│                   │   │   ├── home/
│                   │   │   │   ├── BottomNavRoute.kt   🆕
│                   │   │   │   ├── HomeScreen.kt      (NavDisplay 중심)
│                   │   │   │   └── ...
│                   │   │   └── landing/
│                   │   └── theme/
│                   └── navigation/
│                       ├── Routes.kt                  ← 모든 NavKey 정의
│                       └── nav3/
│                           ├── NavigationState.kt    ← Multi-backstack
│                           ├── PhogalEntryProvider.kt ← EntryProviderScope<NavKey>
│                           └── SharedTransitionKeys.kt
```

---

## 🙏 감사의 말

이 현대화 여정은 Android 생태계의 빠른 변화를 **따라가지 못하는 레퍼런스는 오히려 독이 된다**는 문제의식에서 시작되었습니다. 2023년에 작성된 Phogal을 그대로 두었다면, Kotlin 2.0 이행기의 많은 개발자들이 이 코드를 "최신 레퍼런스"로 오해했을 것입니다.

AOSP navigation3 소스 코드, Google 공식 Nav3 1.1.0 마이그레이션 가이드(2026-04-16), 그리고 Jetpack Compose 팀이 공개한 [nav3-recipes 레포지토리](https://github.com/android/nav3-recipes)가 실제 API 정합성 검증에 결정적이었습니다.

---

## 🔗 참고 자료

- [Navigation 3 공식 문서](https://developer.android.com/guide/navigation/navigation-3)
- [Nav2 → Nav3 마이그레이션 가이드](https://developer.android.com/guide/navigation/navigation-3/migration-guide)
- [Scenes / SceneStrategy 가이드](https://developer.android.com/guide/navigation/navigation-3/custom-layouts)
- [Shared Elements in Compose](https://developer.android.com/develop/ui/compose/animation/shared-elements)
- [Android Architecture Guidelines](https://developer.android.com/topic/architecture)
- [Material 3 Adaptive](https://developer.android.com/develop/ui/compose/layouts/adaptive)

---

> **License**: 원 저작자(Lukoh)의 라이선스를 따릅니다. 이 문서는 2023년 8월 원본 Phogal 프로젝트와 2026년 4월 현대화 버전의 **비교 분석**을 위해 작성되었습니다.
