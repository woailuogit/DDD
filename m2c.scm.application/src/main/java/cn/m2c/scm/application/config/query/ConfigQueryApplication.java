package cn.m2c.scm.application.config.query;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.m2c.ddd.common.port.adapter.persistence.springJdbc.SupportJdbcTemplate;
import cn.m2c.scm.application.config.data.bean.ConfigBean;

/**
 * 配置 
 */
@Service
public class ConfigQueryApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigQueryApplication.class);
	
	@Resource
    private SupportJdbcTemplate supportJdbcTemplate;

    public SupportJdbcTemplate getSupportJdbcTemplate() {
        return supportJdbcTemplate;
    }
	
    
	public ConfigBean queryConfigBeanByConfigKey(String configKey) {
		StringBuilder sql = new StringBuilder();
        sql.append(" SELECT * from t_scm_config where config_key = ? ");
        ConfigBean configBean = this.getSupportJdbcTemplate().queryForBean(sql.toString(), ConfigBean.class, configKey);
		return configBean;
	}

}
