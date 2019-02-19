/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.moviefun.movies;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import java.util.List;

@Repository
public class MoviesBean {

    @PersistenceContext
    @Qualifier("moviesEntityManagerFactory")
    private EntityManager moviesEntityManagerFactory;

    public Movie find(Long id) {
        return moviesEntityManagerFactory.find(Movie.class, id);
    }


    public void addMovie(Movie movie) {
        moviesEntityManagerFactory.persist(movie);
    }

    @Transactional
    public void editMovie(Movie movie) {
        moviesEntityManagerFactory.merge(movie);
    }

    @Transactional
    public void deleteMovie(Movie movie) {
        moviesEntityManagerFactory.remove(movie);
    }

    @Transactional
    public void deleteMovieId(long id) {
        Movie movie = moviesEntityManagerFactory.find(Movie.class, id);
        deleteMovie(movie);
    }

    public List<Movie> getMovies() {
        CriteriaQuery<Movie> cq = moviesEntityManagerFactory.getCriteriaBuilder().createQuery(Movie.class);
        cq.select(cq.from(Movie.class));
        return moviesEntityManagerFactory.createQuery(cq).getResultList();
    }

    public List<Movie> findAll(int firstResult, int maxResults) {
        CriteriaQuery<Movie> cq = moviesEntityManagerFactory.getCriteriaBuilder().createQuery(Movie.class);
        cq.select(cq.from(Movie.class));
        TypedQuery<Movie> q = moviesEntityManagerFactory.createQuery(cq);
        q.setMaxResults(maxResults);
        q.setFirstResult(firstResult);
        return q.getResultList();
    }

    public int countAll() {
        CriteriaQuery<Long> cq = moviesEntityManagerFactory.getCriteriaBuilder().createQuery(Long.class);
        Root<Movie> rt = cq.from(Movie.class);
        cq.select(moviesEntityManagerFactory.getCriteriaBuilder().count(rt));
        TypedQuery<Long> q = moviesEntityManagerFactory.createQuery(cq);
        return (q.getSingleResult()).intValue();
    }

    public int count(String field, String searchTerm) {
        CriteriaBuilder qb = moviesEntityManagerFactory.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        Root<Movie> root = cq.from(Movie.class);
        EntityType<Movie> type = moviesEntityManagerFactory.getMetamodel().entity(Movie.class);

        Path<String> path = root.get(type.getDeclaredSingularAttribute(field, String.class));
        Predicate condition = qb.like(path, "%" + searchTerm + "%");

        cq.select(qb.count(root));
        cq.where(condition);

        return moviesEntityManagerFactory.createQuery(cq).getSingleResult().intValue();
    }

    public List<Movie> findRange(String field, String searchTerm, int firstResult, int maxResults) {
        CriteriaBuilder qb = moviesEntityManagerFactory.getCriteriaBuilder();
        CriteriaQuery<Movie> cq = qb.createQuery(Movie.class);
        Root<Movie> root = cq.from(Movie.class);
        EntityType<Movie> type = moviesEntityManagerFactory.getMetamodel().entity(Movie.class);

        Path<String> path = root.get(type.getDeclaredSingularAttribute(field, String.class));
        Predicate condition = qb.like(path, "%" + searchTerm + "%");

        cq.where(condition);
        TypedQuery<Movie> q = moviesEntityManagerFactory.createQuery(cq);
        q.setMaxResults(maxResults);
        q.setFirstResult(firstResult);
        return q.getResultList();
    }

    public void clean() {
        moviesEntityManagerFactory.createQuery("delete from Movie").executeUpdate();
    }
}
