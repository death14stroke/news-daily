package com.death14stroke.newsdaily.data.model

import android.view.View
import com.blongho.country_data.Country
import com.death14stroke.newsloader.data.model.News

/**
 * Listener when news image is clicked to view larger
 */
typealias ViewImageListener = (view: View, imageUrl: String) -> Unit
/**
 * Listener when news url is clicked to view in browser
 */
typealias OpenNewsListener = (url: String) -> Unit
/**
 * Listener when share button is clicked to share news via socials
 */
typealias ShareNewsListener = (news: News) -> Unit
/**
 * Listener when country from preferences is clicked
 */
typealias CountryClickListener = (country: Country) -> Unit