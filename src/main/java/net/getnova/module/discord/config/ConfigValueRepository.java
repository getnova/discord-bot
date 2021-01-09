package net.getnova.module.discord.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigValueRepository extends JpaRepository<ConfigValue, ConfigValue.Key> {

}
