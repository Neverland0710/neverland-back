package projcet.neverland.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projcet.neverland.entity.TextConversation;

import java.util.List;

@Repository
public interface TextConversationRepository extends JpaRepository<TextConversation, String> {

    List<TextConversation> findByAuthKeyIdOrderBySentAtAsc(String authKeyId);
}