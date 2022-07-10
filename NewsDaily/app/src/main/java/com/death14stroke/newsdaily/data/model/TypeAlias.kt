package com.death14stroke.newsdaily.data.model

import android.view.View
import com.blongho.country_data.Country
import com.death14stroke.newsloader.data.model.News

typealias ViewImageListener = (view: View, imageUrl: String) -> Unit
typealias OpenNewsListener = (url: String) -> Unit
typealias ShareNewsListener = (news: News) -> Unit
typealias CountryClickListener = (country: Country) -> Unit