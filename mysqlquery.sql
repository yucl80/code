use test;
select l.id + 1 as start, min(fr.id) - 1 as stop
from testa as l
    left outer join testa as r on l.id = r.id - 1
    left outer join testa as fr on l.id < fr.id
where r.id is null and fr.id is not null
group by l.id, r.id;

select l.id + 1 as start
from testa as l
  left outer join testa as r on l.id + 1 = r.id
where r.id is null;

select start, stop from (
  select m.id + 1 as start,
    (select min(id) - 1 from testa as x where x.id > m.id) as stop
  from testa as m
    left outer join testa as r on m.id = r.id - 1
  where r.id is null
) as x
where stop is not null;

SELECT (t1.id + 1) as gap_starts_at
FROM testa t1
WHERE NOT EXISTS (SELECT t2.id FROM testa t2 WHERE t2.id = t1.id + 1);


SELECT (t1.id + 1) as gap_starts_at,  (SELECT MIN(t3.id) -1 FROM testa t3 WHERE t3.id > t1.id) as gap_ends_at
FROM testa t1
WHERE NOT EXISTS (SELECT t2.id FROM testa t2 WHERE t2.id = t1.id + 1)
HAVING gap_ends_at IS NOT NULL;

insert into sequence(id) values
    ('a'), ('b'), ('c'), ('e'),
    ('f'), ('g'), ('l'), ('m'), ('n');

select start, stop from (
    select char(ascii(m.id) + 1) as start,
        (select char(min(ascii(id)) - 1) from sequence as x where x.id > m.id) as stop
    from sequence as m
        left outer join sequence as r on ascii(m.id) = ascii(r.id) - 1
    where r.id is null
) as x
where stop <> '';


select id, count(*) from sequence
group by id
having count(*) > 1;


https://www.xaprb.com/blog/2005/12/06/find-missing-numbers-in-a-sequence-with-sql/
