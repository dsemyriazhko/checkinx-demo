package com.checkinx.demo2.models

import java.util.UUID

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "pets")
class Pet {
    @Id
    @GeneratedValue
    @Column(name = "id")
    var id: UUID? = null

    @Column(name = "name")
    var name: String? = null

    @Column(name = "location")
    var location: String? = null

    @Column(name = "age")
    var age: Int? = null
}
