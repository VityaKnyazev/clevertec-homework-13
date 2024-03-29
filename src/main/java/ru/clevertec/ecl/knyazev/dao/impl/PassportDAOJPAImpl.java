package ru.clevertec.ecl.knyazev.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.clevertec.ecl.knyazev.dao.PassportDAO;
import ru.clevertec.ecl.knyazev.dao.exception.DAOException;
import ru.clevertec.ecl.knyazev.data.domain.pagination.Paging;
import ru.clevertec.ecl.knyazev.entity.Passport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PassportDAOJPAImpl implements PassportDAO {

    private static final String FIND_BY_UUID_QUERY = "SELECT p FROM Passport p WHERE p.uuid = :uuid";
    private static final String FIND_ALL_QUERY = "SELECT p FROM Passport p";
    private static final String REMOVE_QUERY = "DELETE FROM Passport p WHERE p.uuid = :passportUUID";

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Passport> findByUUID(UUID uuid) {

        Passport passport = null;

        try {
            passport = entityManager.createQuery(FIND_BY_UUID_QUERY, Passport.class)
                    .setParameter("uuid", uuid)
                    .getSingleResult();
        } catch (IllegalArgumentException | IllegalStateException | PersistenceException e) {
            log.error(String.format("%s%s: %s",
                    DAOException.ENTITY_NOT_FOUND,
                    uuid,
                    e.getMessage()), e);
        }
        return passport != null
                ? Optional.of(passport)
                : Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Passport> findAll(Paging paging) throws DAOException {

        List<Passport> passports = new ArrayList<>();

        try {
            passports = entityManager.createQuery(FIND_ALL_QUERY, Passport.class)
                    .setFirstResult(paging.getOffset())
                    .setMaxResults(paging.getLimit())
                    .getResultList();
        } catch (IllegalArgumentException | PersistenceException e) {
            throw new DAOException(String.format("%s: %s",
                    DAOException.FIND_ALL_ERROR,
                    e.getMessage()), e);
        }
        return passports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Passport save(Passport passport) throws DAOException {

        try {
            entityManager.persist(passport);
        } catch (IllegalArgumentException | PersistenceException e) {
            throw new DAOException(String.format("%s: %s",
                    DAOException.SAVING_ERROR,
                    e.getMessage()), e);
        }
        return passport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Passport update(Passport passport) throws DAOException {

        Passport passportDB = findByUUID(passport.getUuid())
                .orElseThrow(() -> new DAOException(String.format("%s: %s%s",
                        DAOException.UPDATING_ERROR,
                        DAOException.ENTITY_NOT_FOUND,
                        passport.getUuid())));

        passportDB.setPassportSeries(passport.getPassportSeries());
        passportDB.setPassportNumber(passport.getPassportNumber());

        try {
            entityManager.merge(passportDB);
        } catch (IllegalArgumentException | PersistenceException e) {
            throw new DAOException(String.format("%s: %s",
                    DAOException.UPDATING_ERROR,
                    e.getMessage()), e);
        }
        return passportDB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(UUID passportUUID) throws DAOException {

            try {
                entityManager.createQuery(REMOVE_QUERY)
                        .setParameter("passportUUID", passportUUID)
                        .executeUpdate();
            } catch (IllegalArgumentException | PersistenceException e) {
                throw new DAOException(String.format("%s: %s",
                        DAOException.DELETING_ERROR,
                        e.getMessage()), e);
            }
    }
}
