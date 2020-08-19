package main.service;

import main.api.response.ResponseInit;
import org.springframework.stereotype.Service;

@Service
public class GeneralService {

    public ResponseInit getInitData() {
        ResponseInit responseInit = new ResponseInit();
        responseInit.setTitle("DevPub");
        responseInit.setSubtitle("Рассказы разработчиков");
        responseInit.setPhone("+7 903 666-44-55");
        responseInit.setEmail("mail@mail.ru");
        responseInit.setCopyright("Дмитрий Сергеев");
        responseInit.setCopyrightFrom("2005");
        return responseInit;
    }
}
