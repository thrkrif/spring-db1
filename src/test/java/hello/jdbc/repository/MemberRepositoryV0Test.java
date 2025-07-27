package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Transactional
@Rollback
class MemberRepositoryV0Test {

    // h2 db에 memberV0, 10000이 생성된 것을 확인 가능하다.
    // 한 번 더 실행하니까 dbcSQLIntegrityConstraintViolationException 에러가 터짐.
    // 이런 경우에는 memberId가 pk로 잡혀있는데 memberV1 등으로 바꿔주면 괜찮다.
    // 테스트를 반복해서 할 수 있는 경우는 뒤에서 더 알아보겠다.
    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        // save
        Member member = new Member("memberV7", 10000);
        repository.save(member);

        // findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        /*
         * 로그가 findMember=Member(memberId=memberV3, money=10000)
         * 이렇게 나오는 이유는 Member 클래스에서 lombok의 @Data를 사용함.
         * @Data 안에는 toString 기능이 오버라이딩 되어있음.
         */
        log.info("member == findMember {}", member == findMember); // false
        /*
         * 우리가 만든 findById 안에서 new Member로 객체를 생성해서 member와 findMember는
         * 서로 다른 인스턴스이다. 근데 아래의 Assertions에서는 왜 True가 나와?
         */
        log.info("member equals findMember {}", member.equals(findMember)); // true

        /*
         * 원래 Member 클래스에 equlas를 오버라이딩 해서 구현해야 하는데 롬복의 @Data를 쓰면
         * EqualsAndHashCode를 자동으로 만들어준다
         */
        // 매번 log 안쓰고 싶으니 Assertions로 검증
        assertThat(findMember).isEqualTo(member);

        // update
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        // delete
        repository.delete(member.getMemberId());
        // 삭제 검증 방법, NoSuchElementException 이거는 우리가 findById에서 설정해 둔
        // 예외를 작성해야함. assertThatThrownBy로 검증한다.
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);
    }
}