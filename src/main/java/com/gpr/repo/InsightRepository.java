package com.gpr.repo;

import com.gpr.domain.Insight;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class InsightRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Insight> rowMapper = (rs, rowNum) -> {
        UUID id = rs.getObject("id", UUID.class);
        UUID appId = rs.getObject("app_id", UUID.class);
        java.sql.Array countriesSqlArray = rs.getArray("countries");
        String[] countriesArray = countriesSqlArray != null ? (String[]) countriesSqlArray.getArray() : new String[0];
        String lang = rs.getString("lang");
        Timestamp runAt = rs.getTimestamp("run_at");
        String model = rs.getString("model");
        String summaryMd = rs.getString("summary_md");
        String summaryJson = rs.getString("summary_json");
        return new Insight(id, appId, List.of(countriesArray), lang,
                runAt != null ? runAt.toInstant() : null, model, summaryMd, summaryJson);
    };

    public InsightRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Insight save(Insight insight) {
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO insights(id, app_id, countries, lang, run_at, model, summary_md, summary_json) " +
                            "VALUES (?,?,?,?,?,?,?,?)");
            ps.setObject(1, insight.getId());
            ps.setObject(2, insight.getAppId());
            ps.setArray(3, insight.getCountries() != null && !insight.getCountries().isEmpty()
                    ? con.createArrayOf("text", insight.getCountries().toArray()) : null);
            ps.setString(4, insight.getLang());
            ps.setTimestamp(5, insight.getRunAt() != null ? Timestamp.from(insight.getRunAt()) : Timestamp.from(Instant.now()));
            ps.setString(6, insight.getModel());
            ps.setString(7, insight.getSummaryMd());
            ps.setObject(8, jsonb(insight.getSummaryJson()));
            return ps;
        });
        return insight;
    }

    public List<Insight> findRecentByApp(UUID appId, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM insights WHERE app_id = ? ORDER BY run_at DESC LIMIT ?",
                rowMapper,
                appId,
                limit);
    }

    private PGobject jsonb(String json) {
        if (json == null) {
            return null;
        }
        PGobject object = new PGobject();
        object.setType("jsonb");
        try {
            object.setValue(json);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to set jsonb value", e);
        }
        return object;
    }
}
