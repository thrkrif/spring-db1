package hello.jdbc.connection;

/*
 * 객체 생성을 할 이유가 없기에 abstract,
 * abstract는 상속이 가능하기에 아예 상속도 못하게 할거면 final로 선언해 줄 것
 */
public abstract class ConnectionConst {
    // 외부에서 쓸 거기 때문에 public
    public static final String URL = "jdbc:h2:tcp://localhost/~/jdbc";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";

}
