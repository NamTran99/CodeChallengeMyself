import com.example.mybase.extensions.fromJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Serializable
data class AnswerResponse(
    val status: String,
    val uuid: String,
    val read_write_token: String? = null,
    val frontend_context_uuid: String,
    val text: String,
    val final: Boolean,
    val text_completed: Boolean,
    val backend_uuid: String,
    val context_uuid: String,
    val media_items: List<JsonElement> = emptyList(),
    val widget_data: List<JsonElement> = emptyList(),
    val knowledge_cards: List<JsonElement> = emptyList(),
    val inline_entity_data: List<JsonElement> = emptyList(),
    val expect_search_results: String,
    val plan: Plan,
    val form: JsonElement? = null,
    val web_results: JsonElement? = null,
    val mode: String,
    val search_focus: String,
    val gpt4: Boolean,
    val display_model: String,
    val attachments: JsonElement? = null,
    val is_pro_reasoning_mode: Boolean,
    val answer_modes: List<JsonElement> = emptyList(),
    val related_queries: List<String> = emptyList()
){
    val isCompletedLoading = final
    val getAnswer get()= text.fromJson<WebResultContainer>()?.answer?: ""
}

@Serializable
data class Plan(
    val goals: List<String> = emptyList(),
    val final: Boolean,
    val channel_uuid: String? = null
)

@Serializable
data class WebResultContainer(
    val answer: String,
    @SerialName("web_results") val webResults: List<WebResult>
)

@Serializable
data class WebResult(
    val name: String,
    val snippet: String,
    val timestamp: String? = null,
    val url: String,
    @SerialName("meta_data") val metaData: MetaData,
    @SerialName("is_attachment") val isAttachment: Boolean,
    @SerialName("is_image") val isImage: Boolean,
    @SerialName("is_code_interpreter") val isCodeInterpreter: Boolean,
    @SerialName("is_knowledge_card") val isKnowledgeCard: Boolean,
    @SerialName("is_navigational") val isNavigational: Boolean,
    @SerialName("is_widget") val isWidget: Boolean,
    val sitelinks: List<String> = emptyList(),
    @SerialName("is_focused_web") val isFocusedWeb: Boolean,
    @SerialName("is_client_context") val isClientContext: Boolean,
    @SerialName("inline_entity_id") val inlineEntityId: String? = null
)

@Serializable
data class MetaData(
    val client: String,
    val date: String? = null,
    @SerialName("domain_name") val domainName: String,
    val description: String? = null,
    val images: List<String> = emptyList(),
    @SerialName("published_date") val publishedDate: String? = null
)
