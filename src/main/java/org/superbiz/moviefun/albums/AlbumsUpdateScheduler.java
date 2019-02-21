package org.superbiz.moviefun.albums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLData;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.sql.Date;


@Configuration
@EnableAsync
@EnableScheduling
public class AlbumsUpdateScheduler {

    private static final long SECONDS = 1000;
    private static final long MINUTES = 60 * SECONDS;

    private final AlbumsUpdater albumsUpdater;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public AlbumsUpdateScheduler(AlbumsUpdater albumsUpdater, DataSource dataSource) {
        this.albumsUpdater = albumsUpdater;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Scheduled(initialDelay = 15 * SECONDS, fixedRate = 2 * MINUTES)
    //@Scheduled(initialDelay = 0, fixedRate = 2 * MINUTES)
    public void run() {
        try {
            logger.debug("Starting albums update");
            updateDecorator();

            logger.debug("Finished albums update");

        } catch (Throwable e) {
            logger.error("Error while updating albums", e);
        }
    }

    private void updateDecorator() throws IOException {
        String sql = "Select started_at from album_scheduler_task limit 0,1";
        RowMapper<Timestamp> rowMapper = (rs, rowNumber) -> {
            Timestamp timestamp = rs.getTimestamp("started_at");
            return timestamp;
        };
        Timestamp dbDate = jdbcTemplate.queryForObject(sql, rowMapper);
        Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
        if (dbDate == null || (now.getTime() - dbDate.getTime()) / MINUTES > 3) {
            getLock();
            albumsUpdater.update();
            releaseLock();
        }
    }

    private void getLock() {
        String sql = "Update album_scheduler_task set started_at = ? ";
        Timestamp timestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        jdbcTemplate.update(sql, timestamp);
    }
    private void releaseLock() {
        String sql = "Update album_scheduler_task set started_at = NULL ";
        jdbcTemplate.update(sql);
    }
}
