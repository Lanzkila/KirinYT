package com.kirinyt.app.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.kirinyt.app.R
import com.kirinyt.app.database.DBManager
import com.kirinyt.app.database.enums.DownloadType
import com.kirinyt.app.database.models.DownloadItem
import com.kirinyt.app.database.models.Format
import com.kirinyt.app.database.models.FormatRecyclerView
import com.kirinyt.app.database.models.ResultItem
import com.kirinyt.app.database.repository.DownloadRepository
import com.kirinyt.app.ui.downloadcard.FormatSelectionBottomSheetDialog.FormatCategory
import com.kirinyt.app.ui.downloadcard.FormatSelectionBottomSheetDialog.FormatSorting
import com.kirinyt.app.ui.downloadcard.FormatTuple
import com.kirinyt.app.ui.downloadcard.MultipleItemFormatTuple
import com.kirinyt.app.util.Extensions.isYoutubeURL
import com.kirinyt.app.util.FileUtil
import com.kirinyt.app.util.FormatUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File

class DownloadCardViewModel(application: Application) : AndroidViewModel(application) {
    var resultItem: ResultItem? = null
        private set

    var downloadItem: DownloadItem? = null
        private set

    fun setDownloadItem(item: DownloadItem?) {
        downloadItem = item
    }

    fun setResultItem(item: ResultItem?) {
        resultItem = item
    }
}