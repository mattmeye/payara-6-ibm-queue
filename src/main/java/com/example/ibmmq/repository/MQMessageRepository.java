package com.example.ibmmq.repository;

import com.example.ibmmq.entity.MQMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MQMessageRepository {

    @Inject
    private EntityManagerFactory emf;

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

    public Optional<MQMessage> findByMessageId(String messageId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MQMessage> query = em.createQuery(
                "SELECT m FROM MQMessage m WHERE m.messageId = :messageId", MQMessage.class);
            query.setParameter("messageId", messageId);
            List<MQMessage> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<MQMessage> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT m FROM MQMessage m", MQMessage.class).getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<MQMessage> findByStatus(MQMessage.MessageStatus status) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MQMessage> query = em.createQuery(
                "SELECT m FROM MQMessage m WHERE m.status = :status", MQMessage.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<MQMessage> findByQueue(String queueName) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MQMessage> query = em.createQuery(
                "SELECT m FROM MQMessage m WHERE m.queueName = :queueName", MQMessage.class);
            query.setParameter("queueName", queueName);
            return query.getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public long countByStatus(MQMessage.MessageStatus status) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(m) FROM MQMessage m WHERE m.status = :status", Long.class);
            query.setParameter("status", status);
            return query.getSingleResult();
        } catch (Exception e) {
            return 0;
        } finally {
            em.close();
        }
    }

    public void delete(MQMessage message) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (em.contains(message)) {
                em.remove(message);
            } else {
                em.remove(em.merge(message));
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to delete message", e);
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    public List<MQMessage> findByStatusWithLimit(MQMessage.MessageStatus status, int maxResults) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MQMessage> query = em.createQuery(
                "SELECT m FROM MQMessage m WHERE m.status = :status", MQMessage.class);
            query.setParameter("status", status);
            query.setMaxResults(maxResults);
            return query.getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<MQMessage> findBackoutMessagesByQueue(String queueName) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MQMessage> query = em.createQuery(
                "SELECT m FROM MQMessage m WHERE m.queueName = :queueName AND m.status = :status ORDER BY m.backoutAt DESC",
                MQMessage.class);
            query.setParameter("queueName", queueName);
            query.setParameter("status", MQMessage.MessageStatus.BACKOUT);
            return query.getResultList();
        } catch (Exception e) {
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public int deleteOldProcessedMessages(int daysOld) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            int result = em.createQuery(
                "DELETE FROM MQMessage m WHERE m.status = :status AND m.processedAt < :cutoffDate")
                .setParameter("status", MQMessage.MessageStatus.PROCESSED)
                .setParameter("cutoffDate", LocalDateTime.now().minusDays(daysOld))
                .executeUpdate();
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return 0;
        } finally {
            em.close();
        }
    }
}