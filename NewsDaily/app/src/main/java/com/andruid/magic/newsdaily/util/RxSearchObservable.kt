package com.andruid.magic.newsdaily.util

import com.miguelcatalan.materialsearchview.MaterialSearchView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RxSearchObservable {
    companion object {
        @JvmStatic
        fun fromView(searchView: MaterialSearchView): Observable<String> {
            val subject = PublishSubject.create<String>()
            searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String): Boolean {
                    subject.onComplete()
                    return true
                }

                override fun onQueryTextChange(text: String): Boolean {
                    subject.onNext(text)
                    return true
                }
            })
            return subject
        }
    }
}