package com.neerajsahu.flux.androidclient.feature.relationship.presentation.intent

import android.net.Uri
import java.io.File

sealed class EditProfileIntent {
    data class BioChanged(val bio: String) : EditProfileIntent()
    data class ImageSelected(val uri: Uri, val file: File) : EditProfileIntent()
    object UpdateProfile : EditProfileIntent()
    object ClearError : EditProfileIntent()
    object ConsumeSuccess : EditProfileIntent()
}

