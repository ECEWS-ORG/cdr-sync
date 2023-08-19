create table if not exists cdr_sync_batch
(
    cdr_sync_batch_id                  int auto_increment
    primary key,
    owner_username                     varchar(255)                            null,
    total_number_of_patients_processed int       default 0                     null,
    total_number_of_patients           int                                     null,
    date_started                       timestamp default CURRENT_TIMESTAMP     not null,
    date_completed                     timestamp default null                  null,
    status                             varchar(255)                            null,
    sync_type                          varchar(255)                            null
);