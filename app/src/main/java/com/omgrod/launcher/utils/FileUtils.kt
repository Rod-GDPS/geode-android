package com.omgrod.launcher.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File

class FileUtils {
    // copied from https://stackoverflow.com/questions/17546101/get-real-path-for-uri-android
    // i am actually very lazy to move this to a separate class
    // hello zmx i moved it
    companion object {
        fun getRealPathFromURI(context: Context, uri: Uri): String? {
            when {
                // DocumentProvider
                DocumentsContract.isDocumentUri(context, uri) -> {
                    when {
                        // ExternalStorageProvider
                        isExternalStorageDocument(uri) -> {
                            return handleExternalStorageDocument(context, uri)
                        }
                        isDownloadsDocument(uri) -> {
                            return handleDownloadsDocument(context, uri)
                        }
                        isMediaDocument(uri) -> {
                            return handleMediaDocument(context, uri)
                        }

                        // TODO: add the geode provider
                    }
                }
                "content".equals(uri.scheme, ignoreCase = true) -> {
                    // Return the remote address
                    if (isGooglePhotosUri(uri)) return uri.lastPathSegment
                    // This happened with a directory
                    if (isExternalStorageDocument(uri)) {
                        return handleExternalStorageDocument(context, uri)
                    }
                    return getDataColumn(context, uri, null, null)
                }
                "file".equals(uri.scheme, ignoreCase = true) -> {
                    return uri.path
                }
            }
            return null
        }

        private fun handleMediaDocument(context: Context, uri: Uri): String? {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            when (type) {
                "image" -> {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                "video" -> {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                "audio" -> {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])
            return getDataColumn(
                context,
                contentUri,
                selection,
                selectionArgs
            )
        }

        private fun handleDownloadsDocument(context: Context, uri: Uri): String? {
            val fileName = getFilePath(context, uri)
            if (fileName != null) {
                return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName
            }
            var id = DocumentsContract.getDocumentId(uri)
            if (id.startsWith("raw:")) {
                id = id.replaceFirst("raw:".toRegex(), "")
                val file = File(id)
                if (file.exists()) return id
            }
            val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
            return getDataColumn(context, contentUri, null, null)
        }

        private fun handleExternalStorageDocument(context: Context, uri: Uri): String {
            val docId: String = if (DocumentsContract.isDocumentUri(context, uri)) {
                DocumentsContract.getDocumentId(uri)
            } else {
                // please please please PLEASE let this be the only other case
                DocumentsContract.getTreeDocumentId(uri)
            }
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            // This is for checking Main Memory
            return if ("primary".equals(type, ignoreCase = true)) {
                if (split.size > 1) {
                    Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else {
                    Environment.getExternalStorageDirectory().toString() + "/"
                }
                // This is for checking SD Card
            } else {
                "storage" + "/" + docId.replace(":", "/")
            }
        }
        private fun getFilePath(context: Context, uri: Uri?): String? {
            var cursor: Cursor? = null
            val projection = arrayOf(
                MediaStore.MediaColumns.DISPLAY_NAME
            )
            try {
                if (uri == null) return null
                cursor = context.contentResolver.query(
                    uri, projection, null, null,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val data = cursor.getString(index)
                    cursor.close()
                    return data
                }
            } catch (e: Exception) {
                cursor?.close()
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                                  selectionArgs: Array<String>?): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                if (uri == null) return null
                cursor = context.contentResolver.query(
                    uri, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    val data = cursor.getString(index)
                    cursor.close()
                    return data
                }
            } catch (e: Exception) {
                cursor?.close()
            }
            return null
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }
    }
}