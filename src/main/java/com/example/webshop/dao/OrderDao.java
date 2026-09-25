package com.example.webshop.dao;

import com.example.webshop.model.Order;
import com.example.webshop.model.OrderItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface OrderDao {
    Long createOrder(Connection tx, Order order) throws SQLException;
    void addItem(Connection tx, Long orderId, OrderItem item) throws SQLException;
    void updateStatus(Connection tx, Long orderId, String status) throws SQLException;

    //  valfri visning:
    List<Order> listByUser(Long userId) throws SQLException;
    List<Order> listByStatus(String status) throws SQLException;

}
