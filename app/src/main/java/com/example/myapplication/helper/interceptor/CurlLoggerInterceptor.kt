package app.swiftmail.data.helper.interceptor

import java.io.IOException
import java.nio.charset.Charset
import java.util.Locale
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer

class CurlLoggerInterceptor : Interceptor {
    private var curlCommandBuilder: StringBuilder = StringBuilder()
    private val UTF8: Charset = Charset.forName("UTF-8")
    private var tag: String? = null

    constructor()

    /**
     * Set logcat tag for curl lib to make it easier to filter curl logs only.
     * @param tag
     */
    constructor(tag: String?) {
        this.tag = tag
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()

        // Reset the StringBuilder for each request
        curlCommandBuilder.clear()
        curlCommandBuilder.append("cURL ")
        curlCommandBuilder.append("-X ")
        curlCommandBuilder.append("${request.method.uppercase(Locale.getDefault())} ")

        // Adding headers
        for (headerName in request.headers.names()) {
            addHeader(headerName, request.header(headerName))
        }

        // Adding request body if present
        val requestBody = request.body
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val contentType = requestBody.contentType()
            val charset = contentType?.charset(UTF8) ?: UTF8

            if (contentType != null) {
                addHeader("Content-Type", contentType.toString())
            }
            val requestBodyString = buffer.readString(charset)
            if (requestBodyString.isNotEmpty()) {
                curlCommandBuilder.append(" -d '$requestBodyString'")
            }
        }

        // Add request URL
        curlCommandBuilder.append(" \"${request.url}\"")
        curlCommandBuilder.append(" -L")

        // Print the cURL command
        CurlPrinter.print(tag, request.url.toString(), curlCommandBuilder.toString())

        return chain.proceed(request)
    }

    private fun addHeader(headerName: String, headerValue: String?) {
        if (headerValue != null) {
            curlCommandBuilder.append("-H \"$headerName: $headerValue\" ")
        }
    }
}
