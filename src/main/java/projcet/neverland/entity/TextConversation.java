package projcet.neverland.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "text_conversation_TB")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextConversation {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "CONVERSATION_ID", nullable = false, length = 36)
    private String conversationId;

    @Column(name = "AUTH_KEY_ID", nullable = false, length = 36)
    private String authKeyId;

    @Column(name = "SENDER", nullable = false)
    @Enumerated(EnumType.STRING)
    private Sender sender;

    @Column(name = "MESSAGE", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "SENT_AT", nullable = false)
    private LocalDateTime sentAt;

    public enum Sender {
        USER, CHATBOT
    }
}
