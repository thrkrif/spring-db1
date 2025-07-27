package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions; // Assertions 쓸 땐 assertj 꺼를 써라?
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class DBConnectionUtilTest {
    @Test
    void connection(){
        Connection connection = DBConnectionUtil.getConnection();
        assertThat(connection).isNotNull();

        /*
         * 테스트 결과에 PASSWORD가 나오지 않는다 민감한 정보는 누가 제외해주는 것인기
         * Slf4j? 아니면 jdbc 자체에서?
         * 로깅 프레임워크(slf4j)가 아니라, `Connection` 객체 자체(정확히는 H2 데이터베이스 드라이버)의
         * 똑똑하고 안전한 설계 덕분이다.
         * Connection.toString에서 민감한 정보를 제외함.
         * Connection은 인터페이스고 실제 구현체는(DB 드라이버)
         * -> connection 객체의 실제 타입은 org.h2.jdbc.JdbcConnection 이다.
         * 실제로 External Libraries에서 확인 가능함./Gradle ~~~ h2db/org/h2/jdbc/JdbcConnection
         * JdbcConnection 클래스에서 toString을 찾아보니
         *  @Override
               public String toString() {
                return getTraceObjectName() + ": url=" + url + " user=" + user;
                } 이렇게 작성되어 있네
         *
         * Connection은 인터페이스이기 때문에 DB 드라이버가 바뀌어도 변경할 필요가 없다.
         */
    }
}
