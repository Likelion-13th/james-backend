package likelion13th.shop.service;

import jakarta.transaction.Transactional;
import likelion13th.shop.DTO.request.OrderCreateRequest;
import likelion13th.shop.DTO.response.OrderResponseDto;
import likelion13th.shop.domain.Item;
import likelion13th.shop.domain.Order;
import likelion13th.shop.domain.User;
import likelion13th.shop.global.api.ErrorCode;
import likelion13th.shop.global.constant.OrderStatus;
import likelion13th.shop.global.exception.GeneralException;
import likelion13th.shop.repository.ItemRepository;
import likelion13th.shop.repository.OrderRepository;
import likelion13th.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private int calculateFinalPrice(int totalPrice, int mileageToUse) {
        int availableMileage = Math.min(mileageToUse, totalPrice);
        int finalPrice = totalPrice - availableMileage;
        return Math.max(finalPrice, 0);
    }

    @Transactional
    public OrderResponseDto createOrder(OrderCreateRequest request, User user){
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(()-> new GeneralException(ErrorCode.ITEM_NOT_FOUND));

        int totalPrice = item.getPrice() * request.getQuantity();
        int mileageToUse = request.getMileageToUse();
        if (mileageToUse > user.getMaxMileage()) {
            throw new GeneralException(ErrorCode.INVAILD_MILEAGE);
        }

        int finalPrice = calculateFinalPrice(totalPrice, mileageToUse);

        Order order = new Order(user, item, request.getQuantity());
        order.setTotalPrice(totalPrice);
        order.setFinalPrice(finalPrice);
        order.setStatus(OrderStatus.PROCESSING);

        user.useMileage(mileageToUse);
        user.addMileage((int) (finalPrice * 0.1));
        user.updateRecentTotal(finalPrice);
        orderRepository.save(order);

        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponseDto::from)
                .orElseThrow(() -> new GeneralException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional
    public List<OrderResponseDto> getAllOrders(User user) {
        return user.getOrders().stream()
                .map(OrderResponseDto::from)
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
            throw new GeneralException(ErrorCode.INVAILD_MILEAGE);
        }
        order.setStatus(OrderStatus.CANCEL);
        user.useMileage((int) (order.getFinalPrice() * 0.1));

        user.addMileage(order.getTotalPrice() - order.getFinalPrice());
        user.updateRecentTotal(-order.getTotalPrice());

        //return OrderResponseDto.from(order);
        //return true;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void updateOrderStatus() {
        List<Order> orders = orderRepository.findByStatusAndCreatedAtBefore(
                OrderStatus.PROCESSING,
                LocalDateTime.now().minusMinutes(1)
        );

        for (Order order : orders) {
            order.setStatus(OrderStatus.COMPLETE);
        }
    }
}