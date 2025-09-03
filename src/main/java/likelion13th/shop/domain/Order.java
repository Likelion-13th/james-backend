package likelion13th.shop.domain;

import jakarta.persistence.*;
import likelion13th.shop.domain.entity.BaseEntity;
import likelion13th.shop.global.constant.OrderStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    @Setter
    private int totalPrice; //기존 주문 내역을 유지하기 위해

    @Column(nullable = false)
    @Setter
    private int finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    //Item, User 와 연관관계 설정
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //생성자 -> 객체 생성될 때 자동으로 실행! 즉 초기 설정을 할 때 사용
    private Order(User user, Item item, int quantity) {
        this.user = user;
        this.item = item;
        this.quantity = quantity;
        this.status = OrderStatus.PROCESSING;
    }

    public static Order create(User user, Item item, int quantity, int totalPrice, int finalPrice) {
        Order order = new Order(user, item, quantity);
        order.totalPrice = totalPrice;
        order.finalPrice = finalPrice;
        return order;
    }

    // 주문 상태 업데이트
    public void updateStatus(OrderStatus status) {
        this.status = status;
    }

    //양방향 편의 메서드
    @SuppressWarnings("lombok")
    public void setUser(User user) {
        this.user = user;
    }
}
