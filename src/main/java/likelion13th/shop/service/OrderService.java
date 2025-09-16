package likelion13th.shop.service;

import jakarta.transaction.Transactional;
import likelion13th.shop.DTO.request.OrderCreateRequest;
import likelion13th.shop.DTO.response.OrderResponse;
import likelion13th.shop.domain.Item;
import likelion13th.shop.domain.Order;
import likelion13th.shop.domain.User;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.constant.OrderStatus;
import likelion13th.shop.global.exception.GeneralException;
import likelion13th.shop.login.service.UserService;
import likelion13th.shop.repository.ItemRepository;
import likelion13th.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, User user){
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(()-> new GeneralException(ErrorCode.ITEM_NOT_FOUND));

        int totalPrice = item.getPrice() * request.getQuantity();
        int mileageToUse = request.getMileageToUse();
        if (mileageToUse > user.getMaxMileage()) {
            throw new GeneralException(ErrorCode.INVAILD_MILEAGE);
        }
        int availableMileage = Math.min(mileageToUse, totalPrice);
        int finalPrice = totalPrice - availableMileage;

        Order order = new Order(user, item, request.getQuantity(), totalPrice, finalPrice);

        user.useMileage(availableMileage);
        user.addMileage((int) (finalPrice * 0.1));
        user.updateRecentTotal(finalPrice);
        user.addOrder(order);
        orderRepository.save(order);

        return OrderResponse.from(order);
    }

    @Transactional
    public List<OrderResponse> getAllOrders(User user) {
        return user.getOrders().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new GeneralException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.COMPLETE || order.getStatus() == OrderStatus.CANCEL) {
            throw  new GeneralException(ErrorCode.ORDER_CANCEL_FAILED);
        }

        User user = order.getUser();

        if (user.getMaxMileage() < (int) (order.getFinalPrice() * 0.1)) {
            throw new GeneralException(ErrorCode.INVALID_MILEAGE);
        }
        order.updateStatus(OrderStatus.CANCEL);
        user.useMileage((int) (order.getFinalPrice() * 0.1));

        user.addMileage(order.getTotalPrice() - order.getFinalPrice());
        user.updateRecentTotal(-order.getTotalPrice());
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void updateOrderStatus() {
        List<Order> orders = orderRepository.findByStatusAndCreatedAtBefore(
                OrderStatus.PROCESSING,
                LocalDateTime.now().minusMinutes(1)
        );

        for (Order order : orders) {
            order.updateStatus(OrderStatus.COMPLETE);
        }
    }
}