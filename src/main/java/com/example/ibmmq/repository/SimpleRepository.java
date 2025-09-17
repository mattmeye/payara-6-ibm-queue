package com.example.ibmmq.repository;

import com.example.ibmmq.entity.MQMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SimpleRepository {

    @Inject
    private EntityManagerFactory emf;

    public List<MQMessage> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT m FROM MQMessage m", MQMessage.class).getResultList();
        } catch (Exception e) {
            return new ArrayList<>(); // Return empty list if DB not available
        } finally {
            em.close();
        }
    }

    public Optional<MQMessage> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            MQMessage message = em.find(MQMessage.class, id);
            return Optional.ofNullable(message);
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public MQMessage save(MQMessage message) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (message.getId() == null) {
                em.persist(message);
            } else {
                message = em.merge(message);
            }
            em.getTransaction().commit();
            return message;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to save message", e);
        } finally {
            em.close();
        }
    }
}