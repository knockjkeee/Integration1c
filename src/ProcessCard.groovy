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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Field final JsonSlurperClassic json = new JsonSlurperClassic()
@Field final String LOG_PREFIX = "[INTEGRATION: SED] "

String HOST_API = "http://localhost/exec-post/"
String HOST_DOCK = "https://tessa-test/"
String METHOD = 'Integration/ProcessCards'

String URL = HOST_API + METHOD
//String URL = "http://localhost/exec-post/Integration/ProcessCards"


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
    static ProcessCardReq prepareStaticData() {

        def cardReq = new ProcessCardReq()
        cardReq.setRequestID(UUID.randomUUID().toString())

        def cardPacket = new CardPacket()
        cardPacket.setMethod(0)
        cardPacket.setProcesses(List.of(UUID.randomUUID().toString())) //todo random?!?

        def pCardReq = new PCardReq()
        pCardReq.setExternalID("ID МТР (ci.idHolder)") //todo (ci.idHolder)
        pCardReq.setDocTypeID(UUID.randomUUID().toString())
        // todo random?? "Передавать значение = Акт технического осмотра"

        // ====  TEST FILES ====
        def files = new Files()
        files.setMethod(0)
        files.setId(UUID.randomUUID().toString()) //todo file.UUID
        files.setName("Tech_act.docx")
        files.setCategoryID("Файлы на подпись")
        files.setContent("UEsDBBQACAAIAICEH1cAAAAAAAAAA==")

        def prepareFiles = List.of(files)
        pCardReq.setFiles(prepareFiles) //add Files card //todo random?!?

        def section = new Sections()

        def documentCommonInfo = new DocumentCommonInfo()
        def fieldsDocumentCommonInfo = new FieldsDocumentCommonInfo()
        fieldsDocumentCommonInfo.setAuthorID(UUID.toString().toString()) // todo Employee. idHolder
        def docDate = formatInstantNowToString(Instant.now(), true)
        fieldsDocumentCommonInfo.setDocDate(docDate)
        fieldsDocumentCommonInfo.setSubject(String.format("Акт технического осмотра по заявке %s от %s", 123, docDate))
        //todo номер заявки
        documentCommonInfo.setFields(fieldsDocumentCommonInfo)

        def gptDocumentCommonInfo = new GptDocumentCommonInfo()
        def fieldsGptDocumentCommonInfo = new FieldsGptDocumentCommonInfo()
        fieldsGptDocumentCommonInfo.setMediaTypeID(UUID.randomUUID().toString())
        // todo random?? "Передавать значение = Электронный"
        fieldsGptDocumentCommonInfo.setIncomingDate(docDate)
        gptDocumentCommonInfo.setFields(fieldsGptDocumentCommonInfo)

        GptFiles gptFiles = new GptFiles()
        gptFiles.setTable(1) //todo collections

        def collect = prepareFiles.collect { it ->
                RowsFieldsGptFiles rowsFieldsGptFiles = new RowsFieldsGptFiles()
                rowsFieldsGptFiles.setAttachmentTypeID(UUID.randomUUID().toString())
                //todo random?? "Передавать значение = Проект"
                rowsFieldsGptFiles.setID(it.getId())
                return rowsFieldsGptFiles
        }

        if (collect.size() > 0) {
            gptFiles.setRows(collect)
            gptFiles.setTable(1)
        } else {
            gptFiles.setTable(0)
        }

        section.setDocumentCommonInfo(documentCommonInfo) // add  DocumentCommonInfo
        section.setGptDocumentCommonInfo(gptDocumentCommonInfo) // add  GptDocumentCommonInfo
        section.setGptFiles(gptFiles) // add  GptFiles
        pCardReq.setSections(section) // add Section

        cardPacket.setCard(pCardReq) //add PCardReq card
        cardReq.setCardPackets(List.of(cardPacket)) //add List<CardPacket> cardPackets

        return cardReq
    }

    /**
     * Форматирования Instant в Строку для Теста
     */
    private static String formatInstantNowToString(Instant instant, boolean onlyDate) {
        String PATTERN_FORMAT = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_FORMAT).withZone(ZoneId.systemDefault());

        return onlyDate ? formatter.format(instant) + "T" : instant.toString();
    }
}

@JsonPropertyOrder(['METHOD', 'Processes', 'Card'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Массив данных Документы
 */
class CardPacket {

    /**
     * Метод обработки
     * Метод обработки документа. Передавать 0 (создать)
     */
    @JsonProperty("METHOD")
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


@JsonPropertyOrder(['METHOD', 'ID', 'Name', 'CategoryID', 'Content'])
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 *    Файлы документа
 */
class Files {

    /**
     * Метод обработки файла
     * Метод обработки файла. Передавать 0 (создать)
     */
    @JsonProperty("METHOD")
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
class GptFiles {

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
    String docDate

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
    String mediaTypeID

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
class RowsFieldsGptFiles {

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
    String TimeStamp

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
 *    Объект ответа
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
        file.setId(obj.UUID)
        file.setName(obj.title)
        file.setContent(content)
        file.setMethod(0)
        files.add(file)
    }
}


/**
 * Выгрузка данных из NSD
 */
private ProcessCardReq prepareRequestToSed() {

    def cardReq = new ProcessCardReq()
    cardReq.setRequestID(UUID.randomUUID().toString())

    def cardPacket = new CardPacket()
    cardPacket.setMethod(0)
    cardPacket.setProcesses(List.of(UUID.randomUUID().toString())) //todo random?!?

    def pCardReq = new PCardReq()
    pCardReq.setExternalID(subject.idHolder)
    pCardReq.setDocTypeID(UUID.randomUUID().toString())
    // todo random?? "Передавать значение = Акт технического осмотра"

    //выгрузка файлов
    List<Files> prepareFiles = new ArrayList<>()

    def filesData = utils.files(subject)
    if (filesData) {
        filesData.each { it ->
            addFilesToRequest(files, it)
        }
    }

    pCardReq.setFiles(prepareFiles) //add Files card //todo random?!?

    def section = new Sections()

    def documentCommonInfo = new DocumentCommonInfo()
    def fieldsDocumentCommonInfo = new FieldsDocumentCommonInfo()
    fieldsDocumentCommonInfo.setAuthorID(user.idHolder)
    def docDate = formatInstantNowToString(Instant.now(), true)
    fieldsDocumentCommonInfo.setDocDate(docDate)
    fieldsDocumentCommonInfo.setSubject(String.format("Акт технического осмотра по заявке %s от %s", 123, docDate))
    //todo номер заявки
    documentCommonInfo.setFields(fieldsDocumentCommonInfo)

    def gptDocumentCommonInfo = new GptDocumentCommonInfo()
    def fieldsGptDocumentCommonInfo = new FieldsGptDocumentCommonInfo()
    fieldsGptDocumentCommonInfo.setMediaTypeID(UUID.randomUUID().toString())
// todo random?? "Передавать значение = Электронный"
    fieldsGptDocumentCommonInfo.setIncomingDate(docDate)
    gptDocumentCommonInfo.setFields(fieldsGptDocumentCommonInfo)

    GptFiles gptFiles = new GptFiles()
    def collect = prepareFiles.collect { it ->
        RowsFieldsGptFiles rowsFieldsGptFiles = new RowsFieldsGptFiles()
        rowsFieldsGptFiles.setAttachmentTypeID(UUID.randomUUID().toString())//todo random?? "Передавать значение = Проект"
        rowsFieldsGptFiles.setID(it.getId())

        return rowsFieldsGptFiles
    }

    if (collect.size() > 0) {
        gptFiles.setRows(collect)
        gptFiles.setTable(1)
    } else {
        gptFiles.setTable(0)
    }

    section.setDocumentCommonInfo(documentCommonInfo) // add  DocumentCommonInfo
    section.setGptDocumentCommonInfo(gptDocumentCommonInfo) // add  GptDocumentCommonInfo
    section.setGptFiles(gptFiles) // add  GptFiles
    pCardReq.setSections(section) // add Section

    cardPacket.setCard(pCardReq) //add PCardReq card
    cardReq.setCardPackets(List.of(cardPacket)) //add List<CardPacket> cardPackets

    return cardReq
}


/**
 * Загрузка данных в NSD
 */
private boolean loadResponseToNSD(ProcessCardResp response, String host) {

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

            def linkDock = host + "/card" + prCard.getID()
            updateData.put("docActLink", linkDock)

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
 * Форматирования Instant в Строку
 */
private static String formatInstantNowToString(Instant instant, boolean onlyDate) {
    String PATTERN_FORMAT = "yyyy-MM-dd";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_FORMAT).withZone(ZoneId.systemDefault());

    return onlyDate ? formatter.format(instant) + "T" : instant.toString();
}


/**
 * ====================  ENTRY POINT  ====================
 */

prepareConnect()
def connect = (HttpURLConnection) new URL(URL).openConnection()
String data = objToJson(ProcessCardReq.prepareStaticData()) //todo test
//String data = objToJson(prepareRequestToSed()) //todo main object
prepareRequestPOST(connect, data)

if (connect.responseCode == 200) {
    def text = connect.inputStream.text
    def response = json.parseText(text) as ProcessCardResp
    response.initMaps()

    def infoText = "${LOG_PREFIX} Получен ответ от СЭД, requestID: ${response.getRequestID()}, код: ${connect?.responseCode}"
    logger.info(infoText)

    if (loadResponseToNSD(response, HOST_DOCK)) {
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

