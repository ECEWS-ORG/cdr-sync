create table if not exists nfc_card_mapper
(
    nfc_card_mapper_id                  int auto_increment
    primary key,
    nfc_card_id                         varchar(255)                            null,
    patient_identifier                  varchar(255)                            null,
    patient_uuid                        varchar(255)                            null,
    patient_phone_no                    varchar(255)                            null,
    date_created                        timestamp default CURRENT_TIMESTAMP     null,
    creator                             varchar(255)                            null
);