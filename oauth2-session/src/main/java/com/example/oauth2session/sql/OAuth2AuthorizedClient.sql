/**
  OAuth2AuthorizedClientService depends on the table definition
  described in "classpath:org/springframework/security/oauth2/client/oauth2-client-schema.sql"

  => JdbcOAuth2AuthorizedClientService 클래스에서 해당 경로의 SQL 문을 사용함.
    그리고 이 파일은 해당 경로의 SQL 문과 같음.
 */
create table oauth2_authorized_client(
  client_registration_id varchar(100) not null,
  principal_name varchar(100) not null,
  access_token_type varchar(100) not null,
  access_token_value blob not null,
  access_token_issued_at timestamp not null,
  access_token_expires_at timestamp not null,
  access_token_scopes varchar(1000) default null,
  refresh_token_value blob default null,
  refresh_token_issued_at timestamp default null,
  created_at timestamp default current_timestamp not null,
  primary key (client_registration_id, principal_name)
);