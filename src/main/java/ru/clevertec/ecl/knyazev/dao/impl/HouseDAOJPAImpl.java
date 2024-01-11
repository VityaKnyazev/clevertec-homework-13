package ru.clevertec.ecl.knyazev.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.clevertec.ecl.knyazev.dao.HouseDAO;
import ru.clevertec.ecl.knyazev.dao.exception.DAOException;
import ru.clevertec.ecl.knyazev.entity.House;
import ru.clevertec.ecl.knyazev.pagination.Paging;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class HouseDAOJPAImpl implements HouseDAO {

    private static final String FIND_ALL_QUERY = "SELECT h FROM House h";

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<House> findByUUID(UUID uuid) {

        House house = null;

        try {
            house = entityManager.find(House.class, uuid);
        } catch (IllegalArgumentException e) {
            log.error(String.format("%s%s: %s",
                    DAOException.ENTITY_NOT_FOUND,
                    uuid,
                    e.getMessage()), e);
        }
        return house != null
                ? Optional.of(house)
                : Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<House> findAll() {
        List<House> houses = new ArrayList<>();

        try {
            houses = entityManager.createQuery(FIND_ALL_QUERY, House.class)
                    .getResultList();
        } catch (IllegalArgumentException | PersistenceException e) {
            log.error(String.format("%s: %s",
                    DAOException.FIND_ALL_ERROR,
                    e.getMessage()), e);
        }
        return houses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<House> findAll(Paging paging) {

        List<House> houses = new ArrayList<>();

        try {
            houses = entityManager.createQuery(FIND_ALL_QUERY, House.class)
                    .setFirstResult(paging.getOffset())
                    .setMaxResults(paging.getLimit())
                    .getResultList();
        } catch (IllegalArgumentException | PersistenceException e) {
            log.error(String.format("%s: %s",
                    DAOException.FIND_ALL_ERROR,
                    e.getMessage()), e);
        }
        return houses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public House save(House house) throws DAOException {

        try {
            entityManager.persist(house);
        } catch (IllegalArgumentException | PersistenceException e) {
            throw new DAOException(String.format("%s: %s",
                    DAOException.SAVING_ERROR,
                    e.getMessage()), e);
        }
        return house;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public House update(House house) throws DAOException {

        House houseDB = findByUUID(house.getUuid())
                .orElseThrow(() -> new DAOException(String.format("%s: %s%s",
                        DAOException.UPDATING_ERROR,
                        DAOException.ENTITY_NOT_FOUND,
                        house.getUuid())));

        houseDB.setUuid(house.getUuid());
        houseDB.setAddress(house.getAddress());
        houseDB.setLivingPersons(house.getLivingPersons());


        try {
            entityManager.merge(houseDB);
        } catch (IllegalArgumentException | PersistenceException e) {
            throw new DAOException(String.format("%s: %s",
                    DAOException.UPDATING_ERROR,
                    e.getMessage()), e);
        }
        return houseDB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(UUID houseUUID) throws DAOException {

        findByUUID(houseUUID).ifPresent(houseDB -> {
            try {
                entityManager.remove(houseDB);
            } catch (IllegalArgumentException | PersistenceException e) {
                throw new DAOException(String.format("%s: %s",
                        DAOException.DELETING_ERROR,
                        e.getMessage()), e);
            }
        });

    }
}
