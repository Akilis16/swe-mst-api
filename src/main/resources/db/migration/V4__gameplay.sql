alter table question add column position integer not null default 0;

update question set position = 1 where id = 'ccaaedf9-19b1-5c03-9a71-aa70b53a7eb0';
update question set position = 2 where id = 'c796a7a4-4f9a-5476-bcf4-4a9148789617';
update question set position = 3 where id = '2b49d60b-4f7e-5805-973c-61a56b8deeca';
update question set position = 4 where id = '0e95cf44-8857-5609-9ede-d6ef4bc5956d';
update question set position = 5 where id = 'ed054f7f-0b8a-50a3-837e-dbdc324a96cc';
update question set position = 6 where id = 'b6634d5d-0292-5942-9247-17af00a1849b';
update question set position = 7 where id = '44659ab9-bdaa-513c-82ea-300a18e451f0';
update question set position = 8 where id = '2a12f2f6-5f70-5b9a-8646-deb3ae2fc830';
update question set position = 9 where id = 'e6d51ac2-147b-5816-926f-c40811b31899';
update question set position = 10 where id = '8ebb004d-f43d-53a4-9f9b-996419cf0e25';
update question set position = 11 where id = 'c3c552cc-f7a9-5a56-8898-f4255c6910af';
update question set position = 12 where id = 'ee3d540d-5844-5dea-8749-3443abfac771';
update question set position = 13 where id = '3a1397fb-5e20-5ee8-ac0d-48c2987c799b';
update question set position = 14 where id = '432fc7a7-4c5f-5044-b74e-b82138bf932b';
update question set position = 15 where id = '14ab3c3f-bdb6-56bf-9bed-60c5d1231f3f';
update question set position = 16 where id = '37018d34-4eb9-59f3-b973-f23a13af15c2';
update question set position = 17 where id = '7ec7ace7-673d-50cb-b928-beea157cd553';
update question set position = 18 where id = 'd0a454e9-d970-5c95-ae46-d62d6d69a41c';
update question set position = 19 where id = 'eee7aa6b-4daf-501c-b0fb-5cceafcaef69';
update question set position = 20 where id = 'fb4435a5-707d-560e-8c63-6c4c1fd907ed';
update question set position = 21 where id = 'b372c6d1-8c87-5d14-ba23-11000a47b218';
update question set position = 22 where id = 'ad2ec1b9-3ac2-5c6e-b62d-434523ffedea';
update question set position = 23 where id = 'c6a58b2d-9fa6-554c-95e5-d3d68ddda722';
update question set position = 24 where id = 'fb10ea1e-447d-5d99-b4a4-31754f391d80';
update question set position = 25 where id = '8514b898-c775-5068-8858-e85ea646e391';
update question set position = 26 where id = '5149db94-7600-53fc-a603-d42ec3d53253';
update question set position = 27 where id = '5ba99113-69e1-5f52-8ff9-a811a111d670';
update question set position = 28 where id = 'f6a184fc-de29-5d41-9c9a-4a9db0763545';

create index ix_question_quiz_position on question (quiz_id, position);

alter table memory_round add column layout jsonb not null default '[]'::jsonb;
alter table memory_round alter column layout drop default;
