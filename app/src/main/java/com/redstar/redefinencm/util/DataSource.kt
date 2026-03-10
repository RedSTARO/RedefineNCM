package com.redstar.redefinencm.util

import android.net.Uri
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.data.Repository
import com.redstar.redefinencm.data.db.DatabaseProvider

@UnstableApi
class RedirectingDataSourceFactory(
    private val defaultFactory: DataSource.Factory,
) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return RedirectingDataSource(defaultFactory.createDataSource())
    }
}

@UnstableApi
class RedirectingDataSource(
    private val actualDataSource: DataSource,
) : DataSource {

    private var currentUri: Uri? = null
    private val repo =
        Repository(DatabaseProvider.getDao(RedefineNCMApplication.getApplicationContext()))

    override fun addTransferListener(transferListener: TransferListener) {
    }

    override fun open(dataSpec: DataSpec): Long {
        if (dataSpec.uri.scheme == "redefinencm" && dataSpec.uri.host == "playbackPlaceHolder") {
            val id = dataSpec.uri.getQueryParameter("id")?.toLong() ?: 0L
            currentUri = repo.getOnlinePlaySongUri(id)
        }
        Log.d("RedirectingDataSource", "Redirecting URI: ${dataSpec.uri} to $currentUri")

        val redirectedSpec = dataSpec.buildUpon().setUri(currentUri!!).build()
        return actualDataSource.open(redirectedSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        return actualDataSource.read(buffer, offset, readLength)
    }

    override fun getUri(): Uri? = currentUri

    override fun close() {
        actualDataSource.close()
    }
}
