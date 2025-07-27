package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.NoSuchElementException;

/*
 * JDBC - DriverManager 사용
 * jdbc 코드를 짤 일은 없지만 한 번 경험은 해보자
 * JPA 사용했을 땐 Repository 클래스에 굳이 작성할 게 없었는데 JDBC 쓰니까 작성할게 참 많아진다.
 */
@Slf4j
public class MemberRepositoryV0 {
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        // Statement : sql 그대로 넣기
        // PreparedStatement : ?(Placeholder)를 이용한 파라미터 바인딩, Statement를 상속받음
        PreparedStatement pstmt = null;


        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            // 위의 ?(placeholder)에 파라미터 바인딩을 해준다.
            // SQL 인젝션 방어
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); // 쿼리 실행 명령어, 반환값도 있다.(영향을 준 row 갯수)
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e; // 클래스 밖으로 던진다. throws SQLException
        } finally { // 항상 호출이 보장되도록 finally를 이용한다.
            // 실제로 tcp/ip 통신하므로 외부 커넥션을 종료시켜 줘야한다.
            // 순서 주의
//            pstmt.close(); // 여기서 exception이 터지면 con.close가 호출이 안되는 문제가 발생 가능성
//            con.close();
            // 위에 처럼 작성하지말고 close라는 메서드를 작성하자.
            close(con, pstmt, null);

        }

        /*
         * 조회
         */

    }

    /*
     * 조회
     */
    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        /*
         * 밖에 null로 먼저 선언하는 이유가 try catch에서 finally를 호출해야해서 밖에다 둔다?
         */
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();// select(조회)는 executeQuery 를 사용한다.
            if (rs.next()) { // 내부에 커서라는게 있는데 rs.next()를 한 번 호출해줘야 실제 데이터부터 시작
                // 처음에는 아무것도 안가르키다가 next를 호출하면 데이터가 있는지 없는지 확인한다.
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else { // rs.next()가 false이니 데이터가 없다는 걸 의미
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }

        } catch (SQLException e){
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }

    }

    /*
     * 수정, 삭제 또한 등록과 마찬가지로 excuteUpdate를 사용하면 된다.
     */
    public void update(String memberId, int money) throws SQLException{
        String sql = "update member set money=? where member_id = ?";

        /*
         * 1. 밖에 null로 먼저 선언하는 이유가 try catch에서 finally를 호출해야해서 밖에다 둔다?
         * 변수의 scope 문제
         * try 안에서 선언하였다면 try 밖으로 빠져나올 때 변수 사라짐 결론적으로 finally에서
         * 어떤 변수인지 몰라서 컴파일 에러가 발생함. 따라서 try 밖에서 변수를 선언해야함
         * 2. 왜 null로 초기화 해두는가?
         * try 블록에서 con = DriverManager.getConnection(...) 코드가 실행되기 전에
         * 예외가 발생했다고 가정 -> 그러면 con 변수에는 아무런 값도 할당되지 않은 상태로
         * finally 블록이 실행된다.
         * 자바 7부터는 1,2번의 문제 + finally에서 자원을 해제하는 복잡함을 해결해주기 위해
         * try-with-resources가 등장함.
         */
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1,money);
            pstmt.setString(2,memberId);
            int resultSize = pstmt.executeUpdate(); // 변경하는 쿼리는 0 or 1이 나오겠지
            log.info("resultSize={}", resultSize);
        }catch (SQLException e){
            log.info("db error", e);
            throw e;
        }finally {
            close(con, pstmt, null);
        }
    }

    /*
     * 삭제
     */
    public void delete(String memberId) throws SQLException{
        String sql = "delete from member where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,memberId);
            pstmt.executeUpdate();
        }catch (SQLException e){
            log.info("db error", e);
            throw e;
        }finally {
            close(con, pstmt, null);
        }

    }

    /*
     * 이거 애플리케이션 서버랑 DB랑 연결하는 방식 1~3이네.
     * 1. DB 커넥션 연결 - Connection
     * 2. SQL 전달 - Statement
     * 3. 결과 응답 - ResultSet
     */
    private void close(Connection con, Statement stmt, ResultSet rs){
        // 닫으려고 할 때 오류가 터지는거라 어찌 해결 방법이 없다. log만 남겨준다.
        // 리소스 정리할 때는 항상 역순으로 해주어야 한다.
        // connection : con -> stmt -> rs
        // close : rs -> stmt -> con
        if (rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (stmt != null){
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (con != null){
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }

    // opt cmd m -> extract method
    private static Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
