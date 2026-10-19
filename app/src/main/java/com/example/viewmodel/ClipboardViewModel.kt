package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiOcrHelper
import com.example.data.ClipItem
import com.example.data.ClipRepository
import com.example.data.ClipboardDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClipboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ClipRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: All, 1: Favorites
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _isProcessingOcr = MutableStateFlow(false)
    val isProcessingOcr: StateFlow<Boolean> = _isProcessingOcr.asStateFlow()

    private var lastCapturedText: String = ""

    val clips: StateFlow<List<ClipItem>> = combine(
        ClipboardDatabase.getDatabase(application).clipDao().getAllClips(),
        _searchQuery,
        _selectedTab,
        _selectedTag
    ) { allItems, query, tab, tag ->
        val filteredByTab = if (tab == 1) allItems.filter { it.isFavorite } else allItems
        val filteredByQuery = if (query.isBlank()) {
            filteredByTab
        } else {
            filteredByTab.filter {
                it.text.contains(query, ignoreCase = true) || it.tags.contains(query, ignoreCase = true)
            }
        }
        if (tag.isNullOrBlank()) {
            filteredByQuery
        } else {
            filteredByQuery.filter { item ->
                item.tags.split(",").map { it.trim() }.contains(tag)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        val clipDao = ClipboardDatabase.getDatabase(application).clipDao()
        repository = ClipRepository(clipDao)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun addClip(text: String, tags: String = "", sourceApp: String = "External") {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.insert(
                ClipItem(
                    text = text.trim(),
                    timestamp = System.currentTimeMillis(),
                    tags = tags,
                    sourceApp = sourceApp
                )
            )
        }
    }

    fun captureSystemClipboard(context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = clipboard?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0)?.text?.toString()
                if (!text.isNullOrBlank() && text != lastCapturedText) {
                    lastCapturedText = text
                    addClip(text, tags = "auto", sourceApp = "System")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun extractTextFromScreenshot(bitmap: Bitmap, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isProcessingOcr.value = true
            try {
                val extractedText = GeminiOcrHelper.extractTextFromBitmap(bitmap)
                if (extractedText.isNotBlank()) {
                    repository.insert(
                        ClipItem(
                            text = extractedText,
                            timestamp = System.currentTimeMillis(),
                            tags = "screenshot,ocr",
                            sourceApp = "Screenshot"
                        )
                    )
                    onResult(true, "Successfully extracted text from screenshot!")
                } else {
                    onResult(false, "No text found in the image.")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "OCR failed.")
            } finally {
                _isProcessingOcr.value = false
            }
        }
    }

    fun toggleFavorite(item: ClipItem) {
        viewModelScope.launch {
            repository.update(item.copy(isFavorite = !item.isFavorite))
        }
    }

    fun updateClipTags(item: ClipItem, newTags: String) {
        viewModelScope.launch {
            repository.update(item.copy(tags = newTags))
        }
    }

    fun deleteClip(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearNonFavorites()
        }
    }
}
