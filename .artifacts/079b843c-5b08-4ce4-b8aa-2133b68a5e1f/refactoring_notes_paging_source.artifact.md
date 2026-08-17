# Refactoring PagingSource with `safeApiCall`

This document outlines the migration of the project's `PagingSource` implementations to use the centralized `safeApiCall` utility.

## Overview

Previously, `PagingSource.load()` functions manually checked `response.isSuccessful`, caught `IOException`/`HttpException`, and performed their own error message extraction. By integrating `safeApiCall`, we have unified the network error handling logic across the entire data layer.

## Why this is better

Applying `safeApiCall` to Paging sources provides several architectural advantages:

### 1. Centralized Error Parsing
`safeApiCall` contains a robust `extractErrorMessage()` helper. Instead of duplicating the logic to parse Unsplash's error JSON in every Paging source, we now perform it in one place. This ensures that the UI consistently receives human-readable messages even for complex backend failures.

### 2. Standardized Exception Mapping
The utility explicitly handles:
- **`IOException`**: Connectivity issues and timeouts.
- **`SerializationException`**: JSON parsing errors (critical for catching backend schema changes).
- **`CancellationException`**: Correctly re-thrown to support Coroutine's structured concurrency, preventing Paging loads from hanging during navigation.

### 3. Declarative Logic with `fold`
By using the `NetworkResult.fold` extension, the `load()` function becomes more readable and exhaustive. It clearly separates the success path from the empty and error paths.

### 4. Rich Error Context via `BackendException`
We introduced `BackendException(code, message)` to bridge the gap between `NetworkResult.Error` and Paging's `LoadResult.Error(Throwable)`. This allows the UI to access the specific HTTP status code if needed for logic like "Retry only on 5xx".

---

## Code Comparison

### Before
```kotlin
override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
    return try {
        val response = api.getUserPhotos(...)
        if (!response.isSuccessful) {
            return LoadResult.Error(HttpException(response))
        }
        val photos = response.body().orEmpty()
        LoadResult.Page(data = photos, ...)
    } catch (io: IOException) {
        LoadResult.Error(io)
    } // ... more catch blocks
}
```

### After (Improved)
```kotlin
override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
    return safeApiCall {
        api.getUserPhotos(...)
    }.fold(
        onSuccess = { photos -> LoadResult.Page(data = photos, ...) },
        onEmpty = { LoadResult.Page(data = emptyList(), ...) },
        onError = { code, msg -> LoadResult.Error(BackendException(code, msg)) },
        onException = { throwable -> LoadResult.Error(throwable) }
    )
}
```

## Summary of Changes
- **`NetworkResult.kt`**: Added `BackendException` and the `fold` functional API.
- **`SafeApiCall.kt`**: Enhanced with `SerializationException` handling and granular logging.
- **`UserPhotosPagingSource.kt`**: Refactored to use the new pattern.
- **`PhotosPagingSource.kt`**: Refactored to use the new pattern.
- **`PopularPhotosPagingSource.kt`**: Refactored to use the new pattern.
