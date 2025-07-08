package projcet.neverland.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projcet.neverland.entity.TextConversation;

import java.util.List;

@Repository
public interface TextConversationRepository extends JpaRepository<TextConversation, String> {

    // 전체 조회 (오름차순) - 기존 방식
    List<TextConversation> findByAuthKeyIdOrderBySentAtAsc(String authKeyId);

    // 페이징 조회 (내림차순) - 새로 추가
    Page<TextConversation> findByAuthKeyIdOrderBySentAtDesc(String authKeyId, Pageable pageable);
}
