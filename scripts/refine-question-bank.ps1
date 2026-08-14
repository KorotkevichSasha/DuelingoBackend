param([string]$Path = "$PSScriptRoot/../src/main/resources/data/questions.json")

$questions = ConvertFrom-Json -InputObject ([IO.File]::ReadAllText((Resolve-Path $Path)))

function Apply-Specs([string]$topic, [string]$difficulty, [string[]]$specs) {
    $items = @($questions | Where-Object { $_.topic -eq $topic -and $_.difficulty -eq $difficulty })
    if ($items.Count -ne 10 -or $specs.Count -ne 10) { throw "Expected ten items for $topic / $difficulty" }
    for ($i = 0; $i -lt 10; $i++) {
        $parts = $specs[$i].Split('|')
        $question = $items[$i]
        if ($question.type -eq 'SENTENCE_CONSTRUCTION') {
            $sentence = $parts[0]
            if ($parts.Count -gt 1 -and $parts[1]) {
                $question.questionText = $parts[1]
            } elseif ($question.questionText -notmatch '[А-Яа-яЁё]') {
                $question.questionText = "Arrange all words to make one correct $topic sentence. Use every word once."
            }
            $question.correctAnswers = @($sentence)
            $question.options = @(($sentence -replace '[.!?]$','').Split(' ', [StringSplitOptions]::RemoveEmptyEntries))
        } else {
            $question.questionText = $parts[0]
            $question.correctAnswers = @($parts[1])
            $question.options = if ($question.type -eq 'FILL_IN_CHOICE') { @($parts[1], $parts[2], $parts[3]) } else { @($parts[1], $parts[1]) }
        }
    }
}

Apply-Specs 'Adjectives and Adverbs' 'EASY' @(
    'The museum guide gave us ___ directions, so nobody got lost.|clear|clearly|confusing',
    'Please speak ___ so the children can follow you. (подсказка: «медленно»)|slowly|slow|quiet',
    'The little dog waited patiently by the door.|Маленькая собака терпеливо ждала у двери.',
    'This suitcase is too ___ for the overhead shelf.|large|largely|softly',
    'Mia smiled ___ when she opened her birthday present. (подсказка: «счастливо»)|happily|happy|happiness',
    'The careful driver stopped safely before the crossing.|Осторожный водитель безопасно остановился перед переходом.',
    'We heard a ___ noise coming from the kitchen.|strange|strangely|quietly',
    'The nurse spoke ___ to the frightened child. (подсказка: «мягко»)|gently|gentle|loud',
    'Our new neighbour is friendly and very helpful.|Наш новый сосед дружелюбный и очень отзывчивый.',
    'Leo finished the simple exercise ___.|quickly|quick|quicker'
)
Apply-Specs 'Adjectives and Adverbs' 'MEDIUM' @(
    'The audience listened ___ while the scientist explained the discovery.|attentively|attentive|attention',
    'The instructions were surprisingly ___ despite the complex equipment. (подсказка: «понятными»)|clear|clearly|clarity',
    'The experienced pilot landed the plane smoothly in heavy rain.|Опытный пилот плавно посадил самолёт во время сильного дождя.',
    'Nora completed the calculation ___ and found the error.|accurately|accurate|accuracy',
    'The room became ___ quiet when the results appeared. (подсказка: «необычно»)|unusually|usual|unusual',
    'The team responded calmly to the unexpected change.|Команда спокойно отреагировала на неожиданное изменение.',
    'His explanation sounded ___, but the evidence was incomplete.|convincing|convincingly|conviction',
    'The old bridge must be inspected ___. (подсказка: «тщательно»)|carefully|careful|careless',
    'Ella spoke confidently during her first public presentation.|Элла уверенно выступила на своей первой публичной презентации.',
    'The software update runs noticeably ___ than the previous version.|faster|fastest|more fast'
)
Apply-Specs 'Adjectives and Adverbs' 'HARD' @(
    'The proposal is financially ___ but environmentally risky.|viable|viably|viability',
    'The witness described the sequence remarkably ___. (подсказка: «точно»)|accurately|accurate|accuracy',
    'The committee remained cautiously optimistic about the revised plan.|Комитет сохранял осторожный оптимизм по поводу пересмотренного плана.',
    'Her argument was ___ structured and supported by independent data.|logically|logical|logic',
    'The two accounts are ___ different, despite their similar openings. (подсказка: «существенно»)|substantially|substantial|substance',
    'The laboratory follows internationally recognised safety standards.|Лаборатория следует международно признанным стандартам безопасности.',
    'The medicine is generally safe when used ___.|appropriately|appropriate|appropriateness',
    'The company responded ___ late to prevent the data loss. (подсказка: «слишком»)|too|enough|such',
    'The newly appointed director addressed the concerns remarkably well.|Недавно назначенный директор удивительно хорошо ответил на замечания.',
    'The results were statistically ___ but practically insignificant.|significant|significantly|significance'
)

Apply-Specs 'Prepositions' 'EASY' @(
    'The keys are ___ the kitchen table.|on|at|into',
    'Our lesson starts ___ 9:00. (подсказка: точное время)|at|in|on',
    'The children are playing in the garden.|Дети играют в саду.',
    'We usually travel ___ train when we visit Warsaw.|by|on|with',
    'Please put the milk ___ the fridge. (подсказка: «внутрь»)|in|on|at',
    'Maya walked across the bridge before sunset.|Майя перешла через мост до заката.',
    'The pharmacy is ___ the bank and the bakery.|between|among|through',
    'Daniel waited ___ the bus stop. (подсказка: место ожидания)|at|to|from',
    'A small lamp hangs above the desk.|Над письменным столом висит маленькая лампа.',
    'I received a postcard ___ my cousin in Spain.|from|of|by'
)
Apply-Specs 'Prepositions' 'MEDIUM' @(
    'The conference has been postponed ___ next Monday.|until|during|since',
    'She divided the presentation ___ three short sections. (подсказка: «на части»)|into|between|through',
    'The cyclist rode along the river for several kilometres.|Велосипедист проехал несколько километров вдоль реки.',
    'Everyone agreed ___ the final version of the schedule.|on|to|at',
    'The cabin is hidden ___ tall pine trees. (подсказка: среди множества объектов)|among|between|across',
    'We arrived at the theatre just before the doors closed.|Мы прибыли в театр прямо перед закрытием дверей.',
    'The new policy applies ___ every member of staff.|to|for|with',
    'He borrowed the reference book ___ the university library. (подсказка: источник)|from|of|at',
    'A narrow path leads through the forest to the lake.|Узкая тропа ведёт через лес к озеру.',
    'The manager spoke ___ behalf of the whole department.|on|in|at'
)

Apply-Specs 'Articles' 'EASY' @(
    'I took ___ umbrella because it was raining.|an|a|the',
    'Leo wants to become ___ engineer. (подсказка: слово начинается с гласного звука)|an|a|the',
    'Maya adopted a small dog from the local shelter.|Майя взяла маленькую собаку из местного приюта.',
    'Could you close ___ window beside you?|the|a|an',
    'We watched ___ moon rise above the lake. (подсказка: уникальный объект)|the|a|an',
    'There is an orange and a banana in my bag.|В моей сумке лежат апельсин и банан.',
    'My aunt works at ___ university in Bristol.|a|an|the',
    'This is ___ first time I have visited London. (подсказка: порядковое числительное)|the|a|an',
    'The children found a coin under the sofa.|Дети нашли монету под диваном.',
    'Noah ordered ___ sandwich and a cup of tea.|a|an|the'
)
Apply-Specs 'Articles' 'MEDIUM' @(
    'Sara can play ___ violin very well.|the|a|an',
    'We spent a week in ___ Netherlands. (подсказка: название страны во множественном числе)|the|a|an',
    'After lunch, we walked along the River Thames.|После обеда мы прогулялись вдоль Темзы.',
    'The meeting begins at ___ end of the corridor.|the|an|a',
    'Omar thanked ___ captain who had supported the team. (подсказка: конкретный человек)|the|a|an',
    'My brother studies chemistry at university.|Мой брат изучает химию в университете.',
    'They stayed at ___ Hilton near the airport.|the|a|an',
    'We had ___ unusually quiet evening at home. (подсказка: гласный звук)|an|a|the',
    'The rich do not always understand the problems of the poor.|Богатые не всегда понимают проблемы бедных.',
    'Her office is on ___ third floor.|the|a|an'
)
Apply-Specs 'Articles' 'HARD' @(
    'The research was carried out in ___ United Arab Emirates.|the|a|an',
    'What ___ extraordinary piece of news! (подсказка: исчисляемое выражение)|an|a|the',
    'The accused was given the benefit of the doubt.|Обвиняемому дали возможность воспользоваться презумпцией невиновности.',
    'She has ___ good knowledge of medieval history.|a|the|an',
    'After years abroad, he sailed across ___ Atlantic Ocean. (подсказка: название океана)|the|a|an',
    'At dawn, the rescue team finally reached the injured climber.|На рассвете спасатели наконец добрались до раненого альпиниста.',
    'This manuscript is believed to date from ___ Middle Ages.|the|a|an',
    'Her promotion came as ___ complete surprise to everyone. (подсказка: исчисляемое значение)|a|the|an',
    'The more evidence we examine, the clearer the pattern becomes.|Чем больше доказательств мы изучаем, тем яснее становится закономерность.',
    'He was appointed ___ chair of the ethics committee.|—|a|the'
)

Apply-Specs 'Conditional Sentences' 'EASY' @(
    'If water ___ below zero degrees, it freezes.|cools|will cool|cooled',
    'If you leave now, you ___ the 8:15 train. (подсказка: результат в будущем)|will catch|caught|would catch',
    'If it rains tomorrow, we will move the picnic indoors.|Если завтра пойдёт дождь, мы перенесём пикник в помещение.',
    'Plants die if they ___ enough water.|do not get|will not get|did not get',
    'If Emma finishes early, she ___ us at the café. (подсказка: реальный план)|will meet|would meet|met',
    'Call me if you need help with the form.|Позвони мне, если тебе понадобится помощь с анкетой.',
    'If the alarm rings, everyone ___ the building immediately.|leaves|would leave|left',
    'You will feel better if you ___ a short break. (подсказка: Present Simple после if)|take|will take|took',
    'If the shop is closed, we will order the book online.|Если магазин будет закрыт, мы закажем книгу через интернет.',
    'I always wear gloves if the weather ___ cold.|is|will be|were'
)
Apply-Specs 'Conditional Sentences' 'MEDIUM' @(
    'If I ___ more free time, I would learn Italian.|had|have|would have',
    'Maya would accept the job if it ___ closer to home. (подсказка: нереальная ситуация)|were|is|will be',
    'If we lived near the sea, we would swim every morning.|Если бы мы жили у моря, мы бы плавали каждое утро.',
    'What would you do if you ___ a wallet in the street?|found|find|would find',
    'If Daniel knew her number, he ___ her now. (подсказка: would + verb)|would call|calls|will call',
    'I would not buy that laptop unless the price dropped.|Я бы не купил тот ноутбук, если бы цена не снизилась.',
    'If the room ___ brighter, it would be easier to work there.|were|will be|is',
    'We could finish today if everyone ___ on one section. (подсказка: Past Simple)|focused|focuses|will focus',
    'If Nora spoke Japanese, she could apply for that position.|Если бы Нора говорила по-японски, она могла бы подать заявку на эту должность.',
    'The journey would take less time if there ___ a direct train.|were|is|will be'
)
Apply-Specs 'Conditional Sentences' 'HARD' @(
    'If they ___ the warning, the accident would not have happened.|had noticed|noticed|would notice',
    'We would have arrived on time if the flight ___ delayed. (подсказка: отрицание в Past Perfect)|had not been|was not|would not be',
    'Had I known about the roadworks, I would have chosen another route.|Если бы я знал о дорожных работах, я бы выбрал другой маршрут.',
    'If Lena had accepted the scholarship, she ___ in Rome now.|would be studying|will study|studied',
    'Were the evidence stronger, the committee ___ the proposal. (подсказка: инверсия во втором типе)|would approve|approved|will approve',
    'If it had not been for your help, we would have missed the deadline.|Если бы не твоя помощь, мы бы пропустили срок.',
    'If the backup system were reliable, the outage yesterday ___ less damage.|would have caused|caused|will cause',
    'Should you require further information, please ___ our support team. (подсказка: формальная инверсия)|contact|contacted|would contact',
    'Had the medicine been tested properly, the side effects might have been detected.|Если бы лекарство испытали должным образом, побочные эффекты могли бы обнаружить.',
    'If I had followed that career path, I ___ abroad for ten years by now.|would have been working|will work|worked'
)

Apply-Specs 'Reported Speech' 'EASY' @(
    'Lina said, “I am tired.” Lina said that she ___ tired.|was|is|has been',
    'Tom said, “I like this song.” Tom said that he ___ the song. (подсказка: сдвиг времени)|liked|likes|will like',
    'Mia said that she was ready to leave.|Мия сказала, что готова уйти.',
    '“We are waiting outside,” they said. They said that they ___ waiting outside.|were|are|had',
    'Ben said, “I cannot swim.” Ben said that he ___ swim. (подсказка: can → could)|could not|cannot|will not',
    'The teacher told us that the test was easy.|Учитель сказал нам, что тест был лёгким.',
    '“I will call tomorrow,” Eva said. Eva said that she ___ call the next day.|would|will|could',
    'Sam said, “I have lost my key.” Sam said that he ___ his key. (подсказка: Present Perfect → Past Perfect)|had lost|has lost|lost',
    'Noah said that he needed a new notebook.|Ноа сказал, что ему нужна новая тетрадь.',
    '“The bus is late,” Ava said. Ava said that the bus ___ late.|was|is|were'
)
Apply-Specs 'Reported Speech' 'MEDIUM' @(
    '“Do you work here?” she asked me. She asked me if I ___ there.|worked|work|had work',
    '“Where did you park?” Leo asked. Leo asked where I ___. (подсказка: Past Perfect)|had parked|did park|park',
    'Nora asked whether the meeting had already started.|Нора спросила, началась ли уже встреча.',
    '“Please close the door,” he said. He asked me ___ the door.|to close|closing|close',
    '“Do not touch the screen,” the technician said. The technician warned us ___ the screen. (подсказка: not to)|not to touch|to not touched|do not touch',
    'The guide told us to meet outside the museum at noon.|Гид сказал нам встретиться у музея в полдень.',
    '“I saw her yesterday,” Max said. Max said that he had seen her ___.|the day before|yesterday|tomorrow',
    '“We are moving next month,” they said. They said that they were moving ___. (подсказка: next → following)|the following month|next month|last month',
    'Ella asked me how long I had lived in Madrid.|Элла спросила меня, как долго я прожил в Мадриде.',
    '“Can you help me?” Amir asked. Amir asked whether I ___ help him.|could|can|will'
)
Apply-Specs 'Reported Speech' 'HARD' @(
    '“You should revise the conclusion,” the editor said. The editor ___ revising the conclusion.|recommended|promised|denied',
    '“I did not leak the document,” she said. She ___ leaking the document. (подсказка: deny + gerund)|denied|refused|admitted',
    'The witness claimed to have seen the suspect leave the building.|Свидетель утверждал, что видел, как подозреваемый покинул здание.',
    '“Yes, I altered the figures,” the accountant said. The accountant ___ altering the figures.|admitted|denied|suggested',
    '“I will resign if the policy remains,” he said. He ___ to resign if the policy remained. (подсказка: threaten + infinitive)|threatened|recommended|denied',
    'The chairperson reminded everyone that the vote was confidential.|Председатель напомнил всем, что голосование было конфиденциальным.',
    '“Why not postpone the launch?” Mira said. Mira ___ postponing the launch.|suggested|ordered|refused',
    '“I wish I had listened to you,” Paul said. Paul ___ not having listened to me. (подсказка: regret + gerund)|regretted|denied|promised',
    'The minister was reported to have left the negotiations early.|Сообщалось, что министр досрочно покинул переговоры.',
    '“You must submit the form today,” she said. She insisted that I ___ the form that day.|submit|submitted|would submitted'
)

Apply-Specs 'Comparatives and Superlatives' 'EASY' @(
    'A train travelling at 180 km/h is ___ than a bus travelling at 90 km/h.|faster|slower|the fastest',
    'The blue suitcase weighs 24 kg; it is ___ than the 12 kg red one.|heavier|lighter|the heaviest',
    'This exercise is easier than the one we completed yesterday.|||',
    'Today the weather is ___ than yesterday: it is sunny and warm.|better|worse|the best',
    'The second documentary kept everyone engaged; it was ___ than the first.|more interesting|less interesting|the most interesting',
    'Our new classroom is quieter than the room beside the cafeteria.|||',
    'The Nile is ___ than the Thames.|longer|shorter|the longest',
    'This old phone lasts two hours; its battery is ___ than the new phone battery.|worse|better|the worst',
    'The well-lit route is safer than the unlit shortcut at night.|||',
    'At $1,200, this laptop is ___ than the $700 model.|more expensive|less expensive|the most expensive'
)
Apply-Specs 'Comparatives and Superlatives' 'MEDIUM' @(
    'Mount Everest is ___ mountain above sea level.|the highest|higher|the lowest',
    'Of the three printers, model C had no failures, so it is ___.|the most reliable|more reliable|the least reliable',
    'Maya gave the clearest explanation in the whole class.|||',
    'Saturday is ___ day at this market; it receives the most visitors.|the busiest|busier|the quietest',
    'The hostel costs $30, less than every other option; it is ___.|the least expensive|less expensive|the most expensive',
    'It was the most challenging task in the final exam.|||',
    'Her final draft had no errors and was ___ of all.|the best|better|the worst',
    'Of all the stations, North Point is ___ from the city centre.|the farthest|farther|the nearest',
    'February is the shortest month of the year.|||',
    'The survey shows that cycling is ___ activity among our students.|the most popular|more popular|the least popular'
)
Apply-Specs 'Comparatives and Superlatives' 'HARD' @(
    'The sooner we leave, the ___ we will arrive.|earlier|earliest|more early',
    'No other runner was ___ as Lina, who finished first.|as fast|faster|the fastest',
    'The more consistently you practise, the more confident you become.|||',
    'The revised process is ___ more efficient than the old one.|considerably|most|very',
    'This is by far ___ proposal the committee has received.|the most practical|more practical|as practical',
    'The less cluttered the slide is, the easier it is to understand.|||',
    'Our final estimate was slightly ___ than the actual cost.|lower|lowest|more low',
    'The two solutions are equally effective; neither is ___ the other.|better than|the best|as better as',
    'The longer the delay lasted, the more impatient the passengers became.|||',
    'Of the available routes, this one is ___ likely to flood.|the least|less|the less'
)

Apply-Specs 'Relative Clauses' 'EASY' @(
    'The tutor ___ helped me is from Canada.|who|which|where',
    'The book ___ contains the exercises is on the desk.|which|who|where',
    'I thanked the student who found my notebook.|||',
    'We met a writer ___ novels have won several awards.|whose|who|which',
    'This is the café ___ our language club meets.|where|who|whose',
    'The film that we watched yesterday was inspiring.|||',
    'The student ___ the teacher invited arrived early.|whom|which|where',
    'The lesson ___ everyone remembered was about travel.|that|where|whose',
    'The device which won the design prize is very affordable.|||',
    'The captain ___ answered first earned a bonus point.|who|which|where'
)
Apply-Specs 'Relative Clauses' 'MEDIUM' @(
    'I spoke to the engineer ___ designed this bridge.|who|which|where',
    'The software ___ we use for lessons works offline.|which|who|whose',
    'The article that you recommended clarified the rule.|||',
    'We visited a school ___ students speak four languages.|whose|which|whom',
    'Do you remember the room ___ the interview took place?|where|who|which',
    'The year when she moved abroad changed her career.|||',
    'The colleague to ___ I sent the report replied immediately.|whom|who|which',
    'Everything ___ he explained was supported by evidence.|that|where|who',
    'The course which starts on Monday is already full.|||',
    'Applicants ___ submit complete forms will receive a reply.|who|which|where'
)
Apply-Specs 'Relative Clauses' 'HARD' @(
    'Dr Patel, ___ led the research, will present the findings.|who|which|that',
    'The new library, ___ opened in May, is accessible at night.|which|who|that',
    'Elena, whom the committee selected, will lead the project.|||',
    'Marcus, ___ first language is German, also speaks Japanese.|whose|who|which',
    'The old theatre, ___ we first met, has been restored.|where|which|that',
    'The deadline, which had already been extended, cannot be changed again.|||',
    'Professor Lee, to ___ I addressed the question, gave a detailed answer.|whom|who|whose',
    'The report, ___ conclusions surprised the board, was published today.|whose|which|who',
    'Our server, which was upgraded last week, is responding faster.|||',
    'My neighbours, ___ moved here from Dublin, are learning Polish.|who|which|that'
)

$prepositionHard = @(
    'Anna is responsible ___ organizing the workshop.|for|of|at',
    'Mark succeeded ___ solving the final puzzle.|in|at|on',
    'Sophie apologized for arriving late to the interview.|||',
    'Daniel insisted ___ checking every source twice.|on|in|for',
    'My sister is interested ___ computational linguistics.|in|on|at',
    'Our teacher congratulated us on passing the exam.|||',
    'The new student is capable ___ explaining the rule clearly.|of|to|for',
    'Their manager objected ___ changing the agreed schedule.|to|on|for',
    'A young scientist benefited from discussing the result with her team.|||',
    'The storm prevented the team ___ travelling that evening.|from|of|to'
)
Apply-Specs 'Prepositions' 'HARD' $prepositionHard

# A free-text exercise must test knowledge, not the learner's ability to guess
# which of several grammatically possible words the author had in mind.  Every
# FILL_IN_INPUT item therefore carries a Russian semantic cue or an exact
# grammar cue without printing the English answer itself.
$inputHints = @{
    'write' = '«писать»; Present Simple для you'
    'practices' = '«заниматься на пианино»; Present Simple для she'
    'discuss' = '«обсуждать»; Present Simple для they'
    'do' = 'вспомогательный глагол Present Simple для you/they'
    'does' = 'вспомогательный глагол Present Simple для he/she'
    'do not' = 'отрицание Present Simple для you/they'
    'does not' = 'отрицание Present Simple для he/she'
    'wrote' = '«написал»; неправильный глагол в Past Simple'
    'practiced' = '«занималась»; правильный глагол в Past Simple'
    'discussed' = '«обсудил»; правильный глагол в Past Simple'
    'did' = 'вспомогательный глагол для вопроса в Past Simple'
    'did not' = 'отрицание в Past Simple'
    'will' = 'вспомогательный глагол Future Simple'
    'will not' = 'отрицание в Future Simple'
    'are' = 'форма to be для you/they'
    'is' = 'форма to be для he/she'
    'are not' = 'отрицательная форма to be для you/they'
    'is not' = 'отрицательная форма to be для he/she'
    'have' = 'вспомогательный глагол Present Perfect для you/they'
    'has' = 'вспомогательный глагол Present Perfect для he/she'
    'have not' = 'отрицание Present Perfect для you/they'
    'has not' = 'отрицание Present Perfect для he/she'
    'should' = '«следует»; совет или рекомендация'
    'could' = '«могла бы»; возможность при условии разрешения'
    'can' = '«могут»; реальная возможность'
    'might' = '«возможно, могли»; неуверенное предположение'
    'may' = '«возможно»; формальное предположение'
    'must' = '«должны»; логический вывод с высокой уверенностью'
    'should have' = '«следовало сделать»; модальный глагол о прошлом'
    'an' = 'неопределённый артикль перед гласным звуком'
    'the' = 'определённый артикль для конкретного или уникального объекта'
    'a' = 'неопределённый артикль перед согласным звуком'
    'at' = 'предлог для точного времени или конкретной точки'
    'in' = 'предлог «в»; внутри места или в составе области'
    'into' = 'предлог направления «внутрь»'
    'among' = '«среди» множества предметов'
    'from' = 'предлог источника «из/от»'
    'to' = 'предлог после objected перед герундием'
    'will catch' = '«успеешь на поезд»; результат в будущем'
    'will meet' = '«встретится с нами»; результат в будущем'
    'take' = '«сделать перерыв»; Present Simple после if'
    'were' = 'нереальное условие с глаголом to be'
    'would call' = '«позвонил бы»; результат нереального условия'
    'focused' = '«сосредоточился»; Past Simple после if'
    'had not been' = 'отрицательная форма Past Perfect'
    'would approve' = '«одобрил бы»; результат инверсии во втором типе'
    'contact' = '«связаться»; начальная форма после please'
    'are written' = '«пишутся»; Present Simple Passive, множественное число'
    'is practiced' = '«на нём занимаются»; Present Simple Passive, единственное число'
    'are discussed' = '«обсуждаются»; Present Simple Passive, множественное число'
    'were written' = '«были написаны»; Past Simple Passive, множественное число'
    'was practiced' = '«на нём занимались»; Past Simple Passive, единственное число'
    'were discussed' = '«были обсуждены»; Past Simple Passive, множественное число'
    'should have been written' = '«должны были быть написаны»; модальная пассивная конструкция'
    'should have been practiced' = '«на нём следовало позаниматься»; модальная пассивная конструкция'
    'should have been discussed' = '«следовало обсудить»; модальная пассивная конструкция'
    'liked' = 'сдвиньте like из Present Simple в Past Simple'
    'could not' = 'сдвиньте cannot в прошедшую форму'
    'had lost' = 'сдвиньте Present Perfect в Past Perfect'
    'had parked' = 'действие произошло до вопроса; используйте Past Perfect'
    'not to touch' = 'отрицательный инфинитив после warned us'
    'the following month' = 'замените next month на «в следующем месяце» в косвенной речи'
    'denied' = '«отрицала»; после глагола используется герундий'
    'threatened' = '«пригрозил»; далее используется инфинитив'
    'regretted' = '«сожалел»; далее используется герундий'
    'slowly' = '«медленно»; требуется наречие'
    'happily' = '«счастливо»; требуется наречие'
    'gently' = '«мягко»; требуется наречие'
    'clear' = '«понятными»; после were требуется прилагательное'
    'unusually' = '«необычно»; наречие усиливает quiet'
    'carefully' = '«тщательно»; требуется наречие'
    'accurately' = '«точно»; требуется наречие'
    'substantially' = '«существенно»; требуется наречие'
    'too' = '«слишком»; усилитель перед late'
    'heavier' = 'сравнительная форма прилагательного heavy'
    'more interesting' = 'сравнительная форма interesting'
    'worse' = 'сравнительная форма bad'
    'the most reliable' = 'превосходная форма reliable'
    'the least expensive' = '«наименее дорогой»; превосходная конструкция'
    'the farthest' = 'превосходная форма far для расстояния'
    'as fast' = 'конструкция равенства as ... as'
    'the most practical' = 'превосходная форма practical'
    'better than' = 'сравнительная конструкция от good'
    'which' = 'относительное местоимение для предмета'
    'where' = 'относительное слово для места'
    'that' = 'относительное местоимение после everything или для предмета'
    'whose' = 'относительное местоимение принадлежности «чьи»'
}

foreach ($question in $questions) {
    $question.questionText = [regex]::Replace(
        $question.questionText,
        '^___ (The|A|An|Our|This|That|These|Those)\b',
        { param($match) '___ ' + $match.Groups[1].Value.ToLowerInvariant() }
    )
    $question.questionText = [regex]::Replace(
        $question.questionText,
        '\bIf (The|My|Their|Our)\b',
        { param($match) 'If ' + $match.Groups[1].Value.ToLowerInvariant() }
    )
    if ($question.topic -eq 'Present Continuous' -and $question.questionText -match 's___ter') {
        $question.questionText = 'My sister ___ practicing the piano right now.'
        $question.correctAnswers = @('is')
    }
    if ($question.topic -eq 'Articles') {
        $question.questionText = $question.questionText.Replace('Ann___', 'Anna ___').Replace('D___niel', 'Daniel ___').Replace('m___ager', 'manager ___').Replace('s___id', 'said ___')
    }
    if ($question.topic -eq 'Passive Voice' -and $question.difficulty -eq 'MEDIUM' -and (($question.questionText -match 'and the notes') -or (($question.correctAnswers -join ' ') -match 'and the notes'))) {
        $question.correctAnswers = @($question.correctAnswers[0].Replace('was ', 'were '))
        $question.options = @($question.options | ForEach-Object { $_.Replace('was ', 'were ') } | Select-Object -Unique)
    }
    if ($question.topic -eq 'Modal Verbs' -and $question.type -eq 'SENTENCE_CONSTRUCTION' -and (($question.correctAnswers -join ' ') -match 'light is red')) {
        $sentence = if ($question.difficulty -eq 'EASY') {
            'Sophie must stop when the traffic light is red.'
        } else {
            'Sophie should have checked the instructions before starting.'
        }
        $question.correctAnswers = @($sentence)
        $question.options = @(($sentence -replace '[.!?]$','').Split(' ', [StringSplitOptions]::RemoveEmptyEntries))
    }
    if ($question.type -eq 'SENTENCE_CONSTRUCTION' -and $question.questionText -eq 'Put the words in the correct order.') {
        $question.questionText = "Arrange all words to make one correct $($question.topic) sentence. Use every word once."
    }
    if ($question.type -eq 'FILL_IN_CHOICE') {
        $question.options = @($question.options | Select-Object -Unique)
    }
    if ($question.type -eq 'FILL_IN_INPUT') {
        $question.options = @()
        $answerKey = ([string]$question.correctAnswers[0]).ToLowerInvariant()
        $hint = $inputHints[$answerKey]
        if (-not $hint) { throw "No free-input hint for answer '$answerKey'" }
        $baseText = $question.questionText -replace '\s*\(подсказка:.*\)\s*$', ''
        $question.questionText = "$baseText (подсказка: $hint)"

        # In defining clauses both variants are standard English.  Do not mark
        # a learner wrong merely because the seed happened to prefer one.
        if ($question.topic -eq 'Relative Clauses' -and $answerKey -in @('which', 'that') -and $baseText -notmatch ',\s*___' -and $baseText -notmatch '^Everything\s') {
            $question.correctAnswers = @('which', 'that')
            $question.questionText = "$baseText (подсказка: относительное местоимение для предмета; допустимы два варианта)"
        }
    }
}

$json = $questions | ConvertTo-Json -Depth 10
[IO.File]::WriteAllText((Resolve-Path $Path), $json, [Text.UTF8Encoding]::new($false))
Write-Output "Refined $($questions.Count) questions."
