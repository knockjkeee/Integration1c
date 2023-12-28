import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.ObjectMapper

import groovy.transform.Field
import groovy.json.JsonSlurperClassic

import javax.net.ssl.*
import java.nio.charset.Charset
import java.security.SecureRandom

@Field final JsonSlurperClassic json = new JsonSlurperClassic()
@Field final String LOG_PREFIX = "[INTEGRATION: SED] "
String URL = 'http://localhost/exec-post/Integration/ProcessCards'


@JsonPropertyOrder(['RequestID', 'CardPackets'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Атрибуты запроса карточки документа в СЭД:
 */
class ProcessCardReq {

    @JsonProperty("RequestID")
    String requestID

    @JsonProperty("CardPackets")
    List<CardPacket> cardPackets

    static ProcessCardReq prapareStaticData() {
        def fields = new Fields()
        fields.setSubject("Акт технического осмотра по заявке REQ-128 от 20.11.2023")

        def documentCommonInfo = new DocumentCommonInfo()
        documentCommonInfo.setFields(fields)

        def file = new Files()
        file.setName("Tech_act.docx")
        file.setMethod(0)
        file.setContent("UEsDBBQACAAIAICEH1cAAAAAAAAAA==")
//        file.setCategoryID()

        def section = new Sections()
        section.setDocumentCommonInfo(documentCommonInfo)
        section.setFiles(List.of(file))

        def pReqCard = new PCardReq()
//        pReqCard.setId()
//        pReqCard.setExternalID()
//        pReqCard.setTypeID()
        pReqCard.setDocTypeID("0604d711-de06-4686-8684-0ca8388f913f")
        pReqCard.setSections(section)

        def process = new Processes()
        process.setProcessID("3ab5296a-6286-4080-a13b-3d05d889d8ee")

        def cardPackets = new CardPacket()
        cardPackets.setMethod(0)
        cardPackets.setProcesses(List.of(process))
        cardPackets.setCard(List.of(pReqCard))

        def obj = new ProcessCardReq()
        obj.setRequestID("d890396-c00e-4016-96e3-c818f75ab93e")

        obj.setCardPackets(List.of(cardPackets))
        return obj

    }
}

@JsonPropertyOrder(['method', 'Processes', 'Card'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Массив данных Документы
 */
class CardPacket {

    @JsonProperty("method")
    int method

    @JsonProperty("Processes")
    List<Processes> processes

    PCardReq Card

}

@JsonPropertyOrder(['ProcessID', 'FdCustomParticipants', 'StageTemplateID', 'RoleID'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Информация о запускаемых процессах
 */
class Processes {

    @JsonProperty("ProcessID")
    String processID

    @JsonProperty("FdCustomParticipants")
    List<CustomParticipants> fdCustomParticipants

    @JsonProperty("StageTemplateID")
    String stageTemplateID

    @JsonProperty("RoleID")
    String roleID

}

@JsonPropertyOrder(['Table', 'Rows',])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *   Коллекционная секция, в которую заполняются участники для запускаемых процессов по карточке
 */
class CustomParticipants {

    @JsonProperty("Table")
    int table

    @JsonProperty("Rows")
    List<String> rows

}


@JsonPropertyOrder(['ID', 'ExternalID', 'TypeID', 'DocTypeID', 'Sections'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *   Информация о карточке документа
 */
class PCardReq {

    @JsonProperty("ID")
    String id

    @JsonProperty("ExternalID")
    String externalID

    @JsonProperty("TypeID")
    String typeID

    @JsonProperty("DocTypeID")
    String docTypeID

    @JsonProperty("Sections")
    Sections sections

}


@JsonPropertyOrder(['DocumentCommonInfo', 'Files',])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Секции карточки документа (атрибуты карточки).
 */
class Sections {

    @JsonProperty("DocumentCommonInfo")
    DocumentCommonInfo documentCommonInfo

    @JsonProperty("Files")
    List<Files> files

}


@JsonPropertyOrder(['Fields'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Основная информация
 */
class DocumentCommonInfo {

    @JsonProperty("Fields")
    Fields fields

}

@JsonPropertyOrder(['Subject'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Структура
 */
class Fields {

    @JsonProperty("Subject")
    /*
    Тема документа. Передавать значение = Акт технического осмотра по заявке Номер заявки от Дата создания заявки
     */
    String subject
}


@JsonPropertyOrder(['Method', 'Name', 'CategoryID', 'Content'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Файлы документа
 */
class Files {

    @JsonProperty("Method")
    int method

    @JsonProperty("Name")
    String name

    @JsonProperty("CategoryID")
    String categoryID

    @JsonProperty("Content")
    String content
}


@JsonPropertyOrder(['RequestID', 'Events', 'Cards'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Атрибуты ответа карточки документа от СЭД:
 */
class ProcessCardResp {

    String RequestID

    List<Event> Events

    List<PRespCard> Cards


    private void updateEvents() {
        List<Event> resp = new ArrayList<>()

        this.Events.forEach { data ->
            def temp = new Event()
            data.each { it ->
                temp.setProperty(it.key, it.value)
            }
            resp.add(temp)
        }
        this.Events = resp
    }

    private void updatePRespCard() {
        List<PRespCard> resp = new ArrayList<>()
        this.Cards.forEach { data ->
            def temp = new PRespCard()
            data.each { it ->
                temp.setProperty(it.key, it.value)
            }
            resp.add(temp)
        }
        this.Cards = resp
    }

    void initMaps() {
        updateEvents()
        updatePRespCard()
    }

}


@JsonPropertyOrder(['EventType', 'TimeStamp', 'Message', 'StackTrace'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *     Структура. Коллекция событий по текущему запросу
 */
class Event {

    int EventType

    String TimeStamp

    String Message

    String StackTrace

}


@JsonPropertyOrder(['ID', 'ExternalID', 'Barcode'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Обьект ответа
 */
class PRespCard {

    String ID

    String ExternalID

    String Barcode
}


/*
 *  Подключение с отключенным SSL сертификатом
 */

private void prepareConnect() {
    def sc = SSLContext.getInstance("SSL")
    def trustAll = [getAcceptedIssuers: {}, checkClientTrusted: { a, b -> }, checkServerTrusted: { a, b -> }]
    sc.init(null, [trustAll as X509TrustManager] as TrustManager[], new SecureRandom())
    def hostnameVerifier = [verify: { hostname, session -> true }] as HostnameVerifier
    HttpsURLConnection.defaultSSLSocketFactory = sc.socketFactory
    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier)
}

/*
 *  Подготовка POST запроса по http
 */

private def prepareRequestPOST(HttpURLConnection connection, String data) {
    byte[] postData = data.getBytes(Charset.forName("utf-8"))
    connection.setDoOutput(true)
    connection.setInstanceFollowRedirects(false)
    connection.setRequestMethod("POST")
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty("charset", "utf-8")
    connection.setRequestProperty("Content-Length", Integer.toString(postData.length))
    connection.setUseCaches(false)
    def outStream = connection.getOutputStream()
    outStream.write(postData)
    outStream.close()
}


/*
 *  Обьект в json
 */

private String objToJson(Object obj) {
    return new ObjectMapper().writer().writeValueAsString(obj);
}


/**
 * Добавление файла из БД к обьекту ответа
 */
def addFilesToRequest(ArrayList<Files> files, obj) {
    if (obj != null) {
        byte[] data = utils.readFileContent(obj)
        def encode = Base64.encoder.encode(data)
        def content = new String(encode)
        Files file = new Files()
        file.setContent(content)
        file.setName(obj.title)
        file.setMethod(0)
        files.add(file)
    }
}


/*
 *  Выгрузка данных из NSD
 */

private ProcessCardReq prepareRequestToSed() {
    //todo prepare data to SED

    def fields = new Fields()
    fields.setSubject("Акт технического осмотра по заявке " + subject .....+" от " subject ....)
    // todo  Акт технического осмотра по заявке №заявки в NSD от Дата создания заявки в NSD” !!! УТОЧНИТЬ
    def docComInfo = new DocumentCommonInfo()
    docComInfo.setFields(fields)

    //выгрузка файлов
    List<Files> files = new ArrayList<>()

    def filesData = utils.files(subject)
    if (filesData) {
        filesData.each { it ->
            addFilesToRequest(files, it)
        }
    }

    def section = new Sections()
    section.setDocumentCommonInfo(docComInfo)
    section.setFiles(files)

    def pReqCard = new PCardReq()
//        pReqCard.setId()  todo null
//        pReqCard.setExternalID()  todo null
//        pReqCard.setTypeID()  todo null
    pReqCard.setDocTypeID(UUID.randomUUID().toString())
    //todo for test Передавать значение = Акт технического осмотра !!! УТОЧНИТЬ
    pReqCard.setSections(section)

    def process = new Processes()
    process.setProcessID(UUID.randomUUID().toString()) //todo for test
//    process.setStageTemplateID()  todo null
//    process.setRoleID() todo null
//    process.setFdCustomParticipants() todo null

    def cardPackets = new CardPacket()
    cardPackets.setMethod(0)
    cardPackets.setProcesses(List.of(process)) //todo !!!!уточнить структуру в дальнейшем
    cardPackets.setCard(pReqCard)

    def obj = new ProcessCardReq()
    obj.setRequestID(UUID.randomUUID().toString())
    obj.setCardPackets(List.of(cardPackets))
    return obj
}


/*
 *  Загрузка данных в NSD
 */

private boolean loadResponseToNSD(ProcessCardResp response) {

    def obj = subject

    if (obj == null) {
        logger.info("${LOG_PREFIX} Обьект для изменения не найден, RequestID ${response.getRequestID()}")
        return false
    }

    boolean isFail = false;
    def cards = response.getCards()

    response.getEvents().eachWithIndex { event, index ->
        String log = "${LOG_PREFIX} ${event.getTimeStamp()}, Messsage - ${event.getMessage()}, StackTrace - ${event.getStackTrace()}"

        if (event.getEventType() == 0) {
            logger.error(log)
            isFail = true
        } else {
            def prCard = cards.get(index)
            Map<Object, Object> updateData = new HashMap<>()
            updateData.put("docActID", prCard.getID())
            updateData.put("barcode", prCard.getBarcode())
            def closure = {
                utils.edit(obj.UUID, updateData)
            }
            api.tx.call(closure)
            log = "${LOG_PREFIX} Обьект Dock обновлен, ID записи: ${prCard.getExternalID()}"
            logger.info(log)
            isFail = false
        }
    }
    return !isFail;
}


prepareConnect()
def connect = (HttpURLConnection) new URL(URL).openConnection()
//String data = objToJson(ProcessCardReq.prapareStaticData()) //todo test
String data = objToJson(prepareRequestToSed()) //todo main object
prepareRequestPOST(connect, data)

if (connect.responseCode == 200) {
    def text = connect.inputStream.text
    def response = json.parseText(text) as ProcessCardResp
    response.initMaps()

    def infoText = "${LOG_PREFIX} Получен ответ от СЭД, requestID: ${response.getRequestID()}, код: ${connect?.responseCode}"
    logger.info(infoText)

    if (loadResponseToNSD(response)) {
        infoText = "${LOG_PREFIX} Данные загружены в NSD, requestID: ${response.getRequestID()}"
        logger.info(infoText)
    } else {
        def errorText = "${LOG_PREFIX} Данные не загружены в NSD, requestID: ${response.getRequestID()}"
        logger.error(errorText)
    }

} else {
    def errorText = "${LOG_PREFIX} Ошибка в запросе при получении ответа от СЭД, код ошибки: ${connect.responseCode}, ошибка: ${connect?.errorStream?.text}"
    logger.error(errorText)
}

