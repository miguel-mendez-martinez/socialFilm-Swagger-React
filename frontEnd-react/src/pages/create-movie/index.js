import { ArrowCircleLeftOutline as Back, BadgeCheckOutline as Check} from '@graywolfai/react-heroicons'
import { useEffect, useState } from 'react'

import { Shell, Link, Separator, Button } from '../../components'

import { useMovie } from '../../hooks'

const backdrop = movie => {
    const backdrop = movie?.resources?.find(res => res?.type === 'BACKDROP')?.url
    const poster = movie?.resources?.find(res => res?.type === 'POSTER')?.url

    return backdrop ? backdrop : poster ? poster : "https://media.istockphoto.com/id/1058727850/es/vector/resumen-antecedentes-brillantes-con-tiras-de-pel%C3%ADcula-en-blanco-y-copia-espacio.jpg?s=1024x1024&w=is&k=20&c=L-04nGGhotGrcmP-74tTbxx12Ox4v82p5z46bbY8EAk="
}
const poster = movie => movie?.resources?.find(res => res?.type === 'POSTER')? movie.resources.find(res => res?.type === 'POSTER').url : "https://img.pikbest.com/backgrounds/20190423/new-movie-release-movie-theater-advertising-poster-background-image_1815063.jpg!w700wp"

const nuevaPeli = (movie, create) => {
    create(movie)
    window.location.href = "http://localhost:3000/"
}

export default function CreateMovie() {
    const {create} = useMovie()

    var movie = {
        title: "",
        tagline: "",
        overview: "",
        genres: [],
        releaseDate: {
            day: -1,
            month: -1,
            year: -1
        },
        keywords: [],
        producers: [],
        crew: [],
        cast: [],
        resources: [],
        budget: 0,
        status: 'PRODUCTION'
    }

    return <Shell className = 'p-4'>
        
        <img style = {{ height: '36rem' }}
             src = { backdrop(movie) }
             alt = { `${movie.title} backdrop` }
             className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' />

        <Link variant = 'primary'
              className = 'rounded-full absolute text-white top-4 left-8 flex items-center pl-2 pr-4 py-2 gap-4'
              to = '/'>
            <Back className = 'w-8 h-8'/>
            <span>Volver</span>
        </Link>

        <Button variant = 'primary'
              className = 'rounded-full absolute text-white top-4 right-8 flex items-center px-2 py-2 gap-4'
              onClick = {(e) => nuevaPeli(movie, create)}>
            <Check className = 'w-8 h-8'/>
            <h1>Done</h1>
        </Button>
            
        <div className = 'mx-auto max-w-screen-2xl p-8'>
                <Header movie = { movie }  />
                <Info movie = { movie }  />
                <Cast />
        </div>
        
    </Shell>
}

function Header({ movie, update }) {
    const [title, setTitle] = useState('')
    const [picture, setPicture] = useState('')
    const [flag, setFlag] = useState(false)

    useEffect(() => {
        setTitle(movie.title);
        setPicture(poster(movie))
    }, [movie.title])

    const handleSubmit = (e) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        movie.title = title
    }

    const handlePicture = (e) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        if(picture === ""){
            setPicture('none')
            let poster = {
                url: picture,
                type: 'POSTER'
            }
            movie.resources.push(poster)
        }else{
            setFlag(true)
            let poster = {
                url: picture,
                type: 'POSTER'
            }
            movie?.resources?.find(res => res?.type === 'POSTER') ? movie.resources.find(res => res?.type === 'POSTER').url = picture : movie.resources.push(poster)
            
        }
    }

    return <header className = 'mt-64 relative flex items-end pb-8 mb-8'>
        <div className='flex flex-col'><img style = {{ aspectRatio: '2/3' }}
             src = { poster(movie) }
             alt = { `${ movie.title } poster` }
             className = 'w-64 rounded-lg shadow-xl z-20' />
             <div className='flex items-center'>
                <input className='mt-2 p-2 border' value={picture} onChange={(e) => setPicture(e.target.value)}></input>
                <Check className = 'w-12 h-12 ml-2 mt-2 hover:bg-grey-700' onClick={handlePicture} style={{cursor: 'pointer'}}/>
             </div>
             </div>
        <hgroup className = 'flex-1'>
            <div className='bg-black bg-opacity-50 backdrop-filter backdrop-blur flex justify-end items-center'>
                <input className = {`w-full bg-black bg-opacity-0 backdrop-filter backdrop-blur text-right text-white text-6xl font-bold p-8`}
                        value = { title }
                        placeholder = "Introduzca un título"
                        onBlur =  {(e) => setTitle(e.target.value)}
                        onChange = {(e) => setTitle(e.target.value)}/>
                <Check className = 'w-16 h-16 text-white hover:bg-grey-700' onClick={handleSubmit} style={{cursor: 'pointer'}}/>
            </div>
            <Tagline movie = { movie } update = {update}/>
        </hgroup>
    </header>
}
function Info({ movie, update }) {
    const [argumento, setArgumento] = useState('')

    useEffect(() => {
        setArgumento(movie.overview);
    }, [movie.overview])

    const handleSubmit = (e) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        movie.overview = argumento
    }

    return <div className = 'grid grid-cols-5 gap-4'>
        <div className = 'col-span-4'>
            <div className='font-bold text-2xl text-white bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 shadow flex items-center'>
                <h2 className = 'p-4'>
                    Argumento
                </h2>
                <Check className = 'w-12 h-12 text-white hover:bg-grey-700'  onClick={handleSubmit} style={{cursor: 'pointer'}}/>
            </div>
            <textarea name = 'comentario' 
                        value={argumento}
                        className="w-full h-3/4 pt-8 p-4"  
                        onChange={(e) => setArgumento(e.target.value)}
                        />
        </div>
        <div className = 'text-right'>
            <dl className = 'space-y-2'>
                <CrewMember movie = { movie } job = 'Director' label = 'Dirección' update = { update }/>
                <CrewMember movie = { movie } job = 'Producer' label = 'Producción' update = { update } />
                <CrewMember movie = { movie } job = 'Screenplay' label = 'Guión' update = { update } />
                <CrewMember movie = { movie } job = 'Original Music Composer' label = 'Banda sonora' update = { update } />
            </dl>
        </div>
    </div>
}
function Cast({ movie }) {
    return <>
        <h2 className = 'mt-16 font-bold text-2xl'>Reparto principal</h2>
        <Separator />
        <ul className = 'w-full grid grid-cols-10 gap-2 overflow-hidden'>
            {
                movie?.cast?.map(person => <CastMember key = { person.name } person = { person }/>)
            }
        </ul>
    </>
}
function Tagline({ movie, update }) {
    const [tagLine, setTagLine] = useState()

    useEffect(() => {
        setTagLine(movie.tagline);
    }, [movie.tagline])

    const handleSubmit = (e) => {
        //se ha cambiado el título asi que llamamos a nuestra API para aplicar los cambios con patch
        movie.tagline = tagLine
    }


    return <div className='flex'>
            <input className={`block text-3xl font-semibold text-black italic w-full px-8 py-4 text-right`}
                value={tagLine}
                placeholder = "Introduzca el slogan"
                onChange = {(e) => setTagLine(e.target.value)}/>
            <Check className = 'w-16 h-16 bg-white hover:bg-grey-700' onClick={handleSubmit} style={{cursor: 'pointer'}}/>
        </div>
}
function CrewMember({ movie, job, label, update }) {

    let oldPeople = movie?.crew?.filter(p => p.job === job)

        return <div>
            <dt className = 'font-bold text-sm'>{ label }</dt>
                { oldPeople?.map((p, index) => <dd className = 'text-sm flex justify-end align-center p-1' key = { `${ job }/${ p.id }` }> { p.name }</dd>) }
            
        </div>
}
function CastMember({ person }) {
    return <li className = 'overflow-hidden'>
        <img src = { person?.picture }
             alt = { `${person.name} profile` }
             className = 'w-full object-top object-cover rounded shadow'
             style = {{ aspectRatio: '2/3' }}/>
        <span className = 'font-bold block'> { person?.name } </span>
        <span className = 'text-sm block'> { person?.character } </span>
    </li>
}