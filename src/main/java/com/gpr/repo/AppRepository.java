package com.gpr.repo;

import com.gpr.domain.App;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AppRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<App> rowMapper = new RowMapper<>() {
        @Override
        public App mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            String packageId = rs.getString("package_id");
            String name = rs.getString("name");
            Timestamp createdAt = rs.getTimestamp("created_at");
            Instant created = createdAt != null ? createdAt.toInstant() : null;
            return new App(id, packageId, name, created);
        }
    };

    public AppRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<App> findByPackageId(String packageId) {
        return jdbcTemplate.query("SELECT * FROM apps WHERE package_id = ?", rs -> {
            if (rs.next()) {
                return Optional.of(rowMapper.mapRow(rs, 0));
            }
            return Optional.empty();
        }, packageId);
    }

    public Optional<App> findById(UUID id) {
        return jdbcTemplate.query("SELECT * FROM apps WHERE id = ?", rs -> {
            if (rs.next()) {
                return Optional.of(rowMapper.mapRow(rs, 0));
            }
            return Optional.empty();
        }, id);
    }

    public App upsert(String packageId, String name) {
        return findByPackageId(packageId).orElseGet(() -> insert(packageId, name));
    }

    public App ensureExists(UUID id, String packageId) {
        return findById(id).orElseGet(() -> insertWithId(id, packageId));
    }

    private App insert(String packageId, String name) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update("INSERT INTO apps(id, package_id, name, created_at) VALUES (?,?,?,?)",
                id, packageId, name, Timestamp.from(now));
        return new App(id, packageId, name, now);
    }

    private App insertWithId(UUID id, String packageId) {
        Instant now = Instant.now();
        jdbcTemplate.update("INSERT INTO apps(id, package_id, name, created_at) VALUES (?,?,?,?) ON CONFLICT (id) DO NOTHING",
                id, packageId, null, Timestamp.from(now));
        return findById(id).orElseGet(() -> new App(id, packageId, null, now));
    }
}
