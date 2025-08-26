package com.wakefern.sbdemo.batch.feeds;

import org.springframework.data.jpa.repository.JpaRepository;


public interface FeedRepository extends JpaRepository<Feed, Long> {
}