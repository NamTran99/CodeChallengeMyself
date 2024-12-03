package app.swiftmail.data.helper.interceptor

import android.util.Log
import app.swiftmail.core.android.common.data.RemoteConfig
import app.swiftmail.core.android.common.data.remoteconfig.RemoteConfigUtils
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NoTokenRequired

class TokenInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val isNoTokenRequired = request.tag(Invocation::class.java)?.method()
            ?.getAnnotation(NoTokenRequired::class.java) != null
        if (isNoTokenRequired) return chain.proceed(request)

        var token = "dd59bd82c5a8dafbaf71e5e96a9275d8"

        RemoteConfig.fetchAndActivate {
            token = RemoteConfigUtils.getEssessnessApiKey()
        }

        if (request.url.host.contains("www.essayness.com")) {
            val bearer = "Bearer $token"
            request = request.newBuilder()
                .addHeader("Authorization", bearer)
                .build()
        }
        return chain.proceed(request)
    }
}
