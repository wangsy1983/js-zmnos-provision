package com.zoom.nos.provision.util.orm.springjdbc;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Service����.
 * 
 * ��װSimpleJdbcTemplate��һЩ������.
 * 
 * @author zhuming
 * 
 */
public abstract class SimpleJdbcSupport {
	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected SimpleJdbcTemplate jdbcTemplate;

	@Autowired
	public void init(DataSource dataSource) {
		jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	/**
	 * �򻯽�ResultSet���䵽Bean�Ķ���.
	 */
	public <T> ParameterizedBeanPropertyRowMapper<T> resultBeanMapper(
			Class<T> clazz) {
		return ParameterizedBeanPropertyRowMapper.newInstance(clazz);
	}

	/**
	 * �򻯽�Bean���䵽SQL�����Ķ���.
	 */
	public BeanPropertySqlParameterSource paramBeanMapper(Object object) {
		return new BeanPropertySqlParameterSource(object);
	}
}
