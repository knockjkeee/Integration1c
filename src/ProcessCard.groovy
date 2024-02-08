import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.ObjectMapper

import groovy.transform.Field
import groovy.json.JsonSlurperClassic

import javax.net.ssl.*
import java.nio.charset.Charset
import java.security.SecureRandom
import java.time.Instant

@Field final JsonSlurperClassic json = new JsonSlurperClassic()
@Field final String LOG_PREFIX = "[INTEGRATION: SED] "
String URL = 'http://localhost/exec-post/Integration/ProcessCards'

/**
 * ====================  REQUEST OBJECT ====================
 */

@JsonPropertyOrder(['RequestID', 'CardPackets'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Атрибуты запроса карточки документа в СЭД:
 */
class ProcessCardReq {

    /**
     * ID запроса
     * Идентификатор запроса. NSD генерирует и передает для синхронизации пакета запроса и ответа
     */
    @JsonProperty("RequestID")
    String requestID

    /**
     * Массив данных Документы
     */
    @JsonProperty("CardPackets")
    List<CardPacket> cardPackets


    /**
     * Тестовый метод заполнения данных
     * @return
     */
    static ProcessCardReq prapareStaticData() {
//        def fields = new FieldsDocumentCommonInfo()
//        fields.setSubject("Акт технического осмотра по заявке REQ-128 от 20.11.2023")
//
//        def documentCommonInfo = new DocumentCommonInfo()
//        documentCommonInfo.setFields(fields)
//
//        def file = new Files()
//        file.setName("Tech_act.docx")
//        file.setMethod(0)
//        file.setContent("UEsDBBQACAAIAICEH1cAAAAAAAAAA==")
////        file.setCategoryID()
//
//        def section = new Sections()
//        section.setDocumentCommonInfo(documentCommonInfo)
//        section.setFiles(List.of(file))
//
//        def pReqCard = new PCardReq()
////        pReqCard.setId()
////        pReqCard.setExternalID()
////        pReqCard.setTypeID()
//        pReqCard.setDocTypeID("0604d711-de06-4686-8684-0ca8388f913f")
//        pReqCard.setSections(section)
//
//        def process = new Processes()
//        process.setProcessID("3ab5296a-6286-4080-a13b-3d05d889d8ee")
//
//        def cardPackets = new CardPacket()
//        cardPackets.setMethod(0)
//        cardPackets.setProcesses(List.of(process))
//        cardPackets.setCard(List.of(pReqCard))
//
//        def obj = new ProcessCardReq()
//        obj.setRequestID("d890396-c00e-4016-96e3-c818f75ab93e")
//
//        obj.setCardPackets(List.of(cardPackets))
//        return obj
        return null
    }
}

@JsonPropertyOrder(['Method', 'Processes', 'Card'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Массив данных Документы
 */
class CardPacket {

    /**
     * Метод обработки
     * Метод обработки документа. Передавать 0 (создать)
     */
    @JsonProperty("Method")
    int method

    /**
     * Информация о запускаемых процессах
     * ID запускаемого процесса
     * Процесс для запуска документа на согласование
     */
    @JsonProperty("Processes")
    List<String> processes

    /**
     * Информация о карточке документа
     */
    @JsonProperty("Card")
    PCardReq card

}


@JsonPropertyOrder(['ExternalID', 'DocTypeID', 'Files', 'Sections'])
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Информация о карточке документа
 */
class PCardReq {

    /**
     * Внешний ID
     * Передавать ID МТР, по которому формируется Акт осмотра
     */
    @JsonProperty("ExternalID")
    String externalID //+

    /**
     * ID типа документа
     * Передавать значение = Акт технического осмотра
     */
    @JsonProperty("DocTypeID")
    String docTypeID //+

    /**
     * Файлы документа
     */
    @JsonProperty("Files")
    List<Files> files

    /**
     * Секции карточки документа
     */
    @JsonProperty("Sections")
    Sections sections
}


@JsonPropertyOrder(['Method', 'ID', 'Name', 'CategoryID', 'Content'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Файлы документа
 */
class Files {

    /**
     * Метод обработки файла
     * Метод обработки файла. Передавать 0 (создать)
     */
    @JsonProperty("Method")
    int method

    /**
     * ID файла
     * Идентификатор файла. NSD генерирует и передает для синхронизации разделов Files и GptFiles.
     * Прописывается также в п.6.3.2
     */
    @JsonProperty("ID")
    String id

    /**
     * Наименование файла
     * Имя файла и его расширение
     */
    @JsonProperty("Name")
    String name

    /**
     * Категория файла
     * Передавать значение = Файлы на подпись
     */
    @JsonProperty("CategoryID")
    String categoryID

    /**
     * Контент файла
     * Сериализованный бинарный контент файла в Base64 c кодировкой UTF-8
     */
    @JsonProperty("Content")
    String content
}


@JsonPropertyOrder(['DocumentCommonInfo', 'GptDocumentCommonInfo', 'GptFiles'])
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Секции карточки документа (атрибуты карточки).
 * Может включать секции двух типов: строковые и коллекционные.
 * В строковых секциях атрибуты содержаться в структуре Fields, в коллекционных в коллекции Rows (набор строк)
 */
class Sections {

    /**
     * Основная информация
     */
    @JsonProperty("DocumentCommonInfo")
    DocumentCommonInfo documentCommonInfo

    /**
     * Информация по документу
     */
    @JsonProperty("GptDocumentCommonInfo")
    GptDocumentCommonInfo gptDocumentCommonInfo

    /**
     * Набор атрибутов для передачи типа вложения к файлу - массив
     */
    @JsonProperty("GptFiles")
    GptFiles gptFiles

}


@JsonPropertyOrder(['Fields'])
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Основная информация
 */
class DocumentCommonInfo {

    /**
     * Структура полей
     */
    @JsonProperty("Fields")
    FieldsDocumentCommonInfo fields

}


@JsonPropertyOrder(['Fields'])
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Информация по документу
 */
class GptDocumentCommonInfo {

    /**
     * Структура полей
     */
    @JsonProperty("Fields")
    FieldsGptDocumentCommonInfo fields
}


@JsonPropertyOrder(['Table', 'Rows'])
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Набор атрибутов для передачи типа вложения к файлу - массив
 */
class GptFiles{

    /**
     * Признак, что секция является коллекционной. Число (от 0 до 2)
     */
    @JsonProperty("Table")
    int table;

    /**
     * Структура. Коллекционная секция
     */
    @JsonProperty("Rows")
    List<RowsFieldsGptFiles> rows;
}


@JsonPropertyOrder(['AuthorID', 'DocDate', 'Subject'])
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 *  Структура для DocumentCommonInfo
 */
class FieldsDocumentCommonInfo {

    /**
     * Автор
     * Автор документа.
     * Передавать значение ID сотрудника в 1C, который инициировал создание Акта осмотра оборудования
     */
    @JsonProperty("AuthorID")
    String authorID

    /**
     * Дата документа
     * Передавать значение текущей даты
     */
    @JsonProperty("DocDate")
    Instant docDate

    /**
     * Тема
     * Тема документа.
     * Передавать значение = Акт технического осмотра по заявке Номер заявки от Дата создания заявки
     */
    @JsonProperty("Subject")
    String subject

}


@JsonPropertyOrder(['MediaTypeID', 'IncomingDate'])
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 *  Структура для GptDocumentCommonInfo
 */
class FieldsGptDocumentCommonInfo {

    /**
     * Носитель оригинала
     * Передавать значение = Электронный
     */
    @JsonProperty("MediaTypeID")
    Instant mediaTypeID

    /**
     * Входящая дата
     * Передавать значение текущей даты
     */
    @JsonProperty("IncomingDate")
    String incomingDate

}


@JsonPropertyOrder(['AttachmentTypeID', 'FileRowID'])
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Структура. Коллекционная секция
 */
class RowsFieldsGptFiles{

    /**
     * Передавать значение = Проект
     */
    @JsonProperty("AttachmentTypeID")
    String attachmentTypeID;

    /**
     * Идентификатор файла. NSD генерирует и передает для синхронизации разделов Files и GptFiles.
     * Прописывается также в п.5.2
     */
    @JsonProperty("ID")
    String ID
}


/**
 * ====================  RESPONSE OBJECT ====================
 */

@JsonPropertyOrder(['RequestID', 'Events', 'Cards'])
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Атрибуты ответа карточки документа от СЭД:
 */
class ProcessCardResp {

    /**
     * ID запроса
     * ID запроса, на который отправляется данный ответ
     */
    String RequestID

    /**
     * Структура. Коллекция событий по текущему запросу
     */
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
/**
 * Структура. Коллекция событий по текущему запросу
 */
class Event {

    /**
     * Тип события
     * 0 = Error = Ошибка. Возвращается в случае критической ошибки, при которой дальнейшая обработка запроса невозможна.
     * 1 = Warn = Предупреждение. Возвращается при незначительных ошибках, не препятствующих обработке запроса.
     * 2 = Info = Информация. Общая информация о ходе выполнения запроса.
     */
    int EventType

    /**
     * Дата, время
     * Момент времени получения запроса
     */
    Instant TimeStamp

    /**
     * Текст ошибки
     * В случае ошибки записывается текст ошибки
     */
    String Message

    /**
     * Трассировка вызова
     * Техническая информация о трассировке ошибки. Заполняется только с типом события Error (0)
     */
    String StackTrace

}


@JsonPropertyOrder(['ID', 'ExternalID', 'Barcode'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Обьект ответа
 */
class PRespCard {

    /**
     * ID карточки в СЭД
     */
    String ID

    /**
     * ID карточки внешней ИС
     * Возвращается, если поле заполнено в карточке
     */
    String ExternalID

    /**
     * Штрих-код карточки документа
     * При создании карточки, СЭД генерирует штрих-код документа и возвращает его в запросе
     */
    String Barcode
}


/**
 * Подключение с отключенным SSL сертификатом
 */
private void prepareConnect() {
    def sc = SSLContext.getInstance("SSL")
    def trustAll = [getAcceptedIssuers: {}, checkClientTrusted: { a, b -> }, checkServerTrusted: { a, b -> }]
    sc.init(null, [trustAll as X509TrustManager] as TrustManager[], new SecureRandom())
    def hostnameVerifier = [verify: { hostname, session -> true }] as HostnameVerifier
    HttpsURLConnection.defaultSSLSocketFactory = sc.socketFactory
    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier)
}

/**
 * Подготовка POST запроса по http
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


/**
 * Обьект в json
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


/**
 * Выгрузка данных из NSD
 */
private ProcessCardReq prepareRequestToSed() {
    //todo prepare data to SED

    def fields = new FieldsDocumentCommonInfo()
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


/**
 * Загрузка данных в NSD
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


/**
 * ====================  ENTRY POINT  ====================
 */

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

