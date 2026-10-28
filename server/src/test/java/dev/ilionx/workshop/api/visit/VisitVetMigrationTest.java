package dev.ilionx.workshop.api.visit;

import liquibase.integration.spring.SpringLiquibase;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.*;

class VisitVetMigrationTest {

    @Test
    void migrationPreservesExistingVisitsWithoutInventingAttribution() throws Exception {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:migration_" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );
        final SpringLiquibase initial = new SpringLiquibase();
        initial.setDataSource(dataSource);
        initial.setChangeLog("classpath:db/changelog/changesets/202507101200-PRD-initial-schema.xml");
        initial.afterPropertiesSet();

        final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("insert into pet_types (id, name) values (1, 'Dog')");
        jdbc.update("insert into owners (id, first_name, last_name) values (1, 'A', 'B')");
        jdbc.update("insert into pets (id, name, birth_date, type_id, owner_id) values (1, 'Pet', '2020-01-01', 1, 1)");
        jdbc.update("insert into visits (id, pet_id, date, description) values (1, 1, '2023-01-01', 'Existing')");

        final SpringLiquibase migration = new SpringLiquibase();
        migration.setDataSource(dataSource);
        migration.setChangeLog("classpath:db/changelog/changesets/202610281200-PRD-visit-vet.xml");
        migration.afterPropertiesSet();

        assertEquals("Existing", jdbc.queryForObject("select description from visits where id = 1", String.class));
        assertNull(jdbc.queryForObject("select vet_id from visits where id = 1", Integer.class));
    }
}
