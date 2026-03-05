package com.cbo.credit_scoring.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Case {
    @Id
    private String caseId;
}
