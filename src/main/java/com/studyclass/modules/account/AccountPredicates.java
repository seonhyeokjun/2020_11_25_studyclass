package com.studyclass.modules.account;

import com.querydsl.core.types.Predicate;
import com.studyclass.modules.tag.Tag;
import com.studyclass.modules.zone.Zone;

import java.util.Set;

public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones){
        QAccount account = QAccount.account;
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }
}
