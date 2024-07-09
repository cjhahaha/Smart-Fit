package com.example.smart_fit

import android.net.Uri

class ModelImagePicked {
    var id =""
    var imageUri: Uri? = null
    var imageUri: String? = null
    var fromInternet = false
    constructor()
    constructor(id: String, imageUri: Uri?, imageUri1: String?, fromInternet: Any) {
        this.id = id
        this.imageUri = imageUri
        this.imageUri = imageUri1
        this.fromInternet = fromInternet
    }


}