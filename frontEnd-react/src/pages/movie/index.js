import { useParams } from 'react-router-dom'
import { ArrowCircleLeftOutline as Back, PencilAltOutline as Edit } from '@graywolfai/react-heroicons'
import ReactPlayer from 'react-player'
import { useState } from 'react'

import { Shell, Link, Separator } from '../../components'

import { useMovie, useComments, useUser } from '../../hooks'

import Disney from './icons/disney_plus.png'
import Play from './icons/google_play.png'
import HBO from './icons/hbo.png'
import ITunes from './icons/itunes.png'
import Netflix from './icons/netflix.png'
import Prime from './icons/prime_video.png'
import Youtube from './icons/youtube.png'
import Estrella from './icons/estrellaEntera.png'
import EstrellaV from './icons/estrellaVacia.png'

import {ListaComents } from '../../components/comment'

const backdrop = movie => {
    const backdrop = movie?.resources?.find(res => res?.type === 'BACKDROP')?.url
    const poster = movie?.resources?.find(res => res?.type === 'POSTER')?.url

    return backdrop ? backdrop : poster
}
const poster = movie => movie?.resources?.find(res => res?.type === 'POSTER')?.url

export default function Movie() {
    const { id } = useParams()
    const movie = useMovie(id).movie
    const user = useUser(localStorage.getItem('email'))

    return <Shell>
        <img style = {{ height: '36rem' }}
             src = { backdrop(movie) }
             alt = { `${movie.title} backdrop` }
             className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' />

        <Link variant = 'primary'
              className = 'rounded-full absolute text-white top-4 left-8 flex items-center pl-2 pr-4 py-2 gap-4'
              to = '/'
        >
            <Back className = 'w-8 h-8'/>
            <span>Volver</span>
        </Link>

        
        {user.user.length === 0 ? null :  user.user.roles[0] === 'ROLE_USER' ? null:
        <Link variant = 'primary'
              className = 'rounded-full absolute text-white top-4 right-8 flex items-center px-2 py-2 gap-4'
              to = {`/movies/${id}/edit`}>
            <Edit className = 'w-8 h-8'/>
        </Link>}
        

        <div className = 'mx-auto max-w-screen-2xl p-8'>
            <Header movie = { movie } />
            <Info movie = { movie } />
            <View movie = { movie } />
            <Cast movie = { movie } />
            <Comments movie = { movie } />
        </div>
        
    </Shell>
}

function Header({ movie }) {
    return <header className = 'mt-64 relative flex items-end pb-8 mb-8'>
        <img style = {{ aspectRatio: '2/3' }}
             src = { poster(movie) }
             alt = { `${ movie.title } poster` }
             className = 'w-64 rounded-lg shadow-xl z-20' />
        <hgroup className = 'flex-1'>
            <h1 className = {`bg-black bg-opacity-50 backdrop-filter backdrop-blur 
                                          text-right text-white text-6xl font-bold
                                          p-8`}>
                { movie.title }
            </h1>
            <Tagline movie = { movie } />
        </hgroup>
    </header>
}
function Info({ movie }) {
    return <div className = 'grid grid-cols-5 gap-4'>
        <div className = 'col-span-4'>
            <h2 className = 'font-bold text-2xl text-white bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 p-4 shadow'>
                Argumento
            </h2>
            <p className = 'pt-8 p-4'>
                { movie.overview }
            </p>
        </div>
        <div className = 'text-right'>
            <dl className = 'space-y-2'>
                <CrewMember movie = { movie } job = 'Director' label = 'Dirección' />
                <CrewMember movie = { movie } job = 'Producer' label = 'Producción' />
                <CrewMember movie = { movie } job = 'Screenplay' label = 'Guión' />
                <CrewMember movie = { movie } job = 'Original Music Composer' label = 'Banda sonora' />
            </dl>
        </div>
    </div>
}
function View({ movie }) {
    return <div className = 'flex gap-4 mt-8'>
        <div className = 'w-80 z-10'>
            <Links movie = { movie } />
        </div>
        <div style = {{
                aspectRatio: '16/9'
             }}
             className = 'flex-1 ml-8 mt-8 bg-pattern-2 flex items-center justify-center z-20'>
            <Trailer movie = { movie } />
        </div>
    </div>
}
function Cast({ movie }) {
    return <>
        <h2 className = 'mt-16 font-bold text-2xl'>Reparto principal</h2>
        <Separator />
        <ul className = 'w-full grid grid-cols-10 gap-2 overflow-hidden'>
            {
                movie?.cast?.slice(0, 10).map(person => <CastMember key = { person.name } person = { person }/>)
            }º
        </ul>
    </>
}
function Comments({ movie }) {
    const { comments, createComment } = useComments({ filter: { film : movie.id } } )

    const [seleccionadas, setSeleccionadas] = useState(0)
    
    const [textoComent, setTextoComent] = useState('')

    const estrellas = [1,2,3,4,5,6,7,8,9,10]

    const handleClick = (event) => setSeleccionadas(event.target.id)

    const handleChange = (event) => setTextoComent(event.target.value)
    

    const publicaComent = (event) => {
        //usando nuestras dos variables de estado, seleccionadas y textoComment, mandaremos esta informacion a createComment para que se cree alli un body y una request acorde

        let comentario = {
            rating: parseInt(seleccionadas),
            user: {
                email: localStorage.getItem('user')
            },
            movie: {
                id: movie.id,
                title: movie.title
            },
            comment: textoComent
        }

        createComment(comentario)

        setSeleccionadas(0)
        setTextoComent('')

    }

    return <>
        <h2 className = 'mt-16 font-bold text-2xl'>Comentarios</h2>
        <Separator />
        <ListaComents comments = {comments} movie = {movie}/>
        <div className='mt-8 flex ' style={{ height: '24rem' }}>
            <div className='p-5 flex flex-col' style={{ width: '25%' }}>
                <p className="font-bold font-serif" style={{ height: '20%' }}>Y a ti, que te ha parecido?</p>
                <div style={{ height: '65%' }}>
                    <div className=" flex" style={{ height: '25px' }}>
                            {estrellas.map((estrella, idx) => 
                                <img src = {idx+1 <= seleccionadas ? Estrella : EstrellaV}
                                id = {idx+1}
                                alt = { `estrella ${idx}` }
                                key = {idx}
                                className = ''
                                style = {{ width: '25px', cursor: 'pointer' }}
                                onClick = {handleClick}/>
                            )}
                    </div>
                </div>
                <button className="w-full border border-red-500 bg-red-600 hover:bg-red-900" style={{ height: '15%'}} onClick = {publicaComent}>Publicar</button>
            </div>
            <div className="h-full ml-12" style={{ width: '75%' }}>
                <form className="h-full">
                    <textarea name = 'comentario' 
                        value={textoComent}
                        className="w-full h-full rounded-md bg-white border p-5 placeholder-gray-400 font-medium'"  
                        placeholder = 'Escribe aqui tu comentario y comparte tu opinión con otros usuarios! Pero por favor, evita hacer spoilers...'
                        onChange={handleChange}
                        />
                </form>
            </div>
        </div>
    </>
}
function Tagline({ movie }) {
    if(movie.tagline) {
        return <q className={`block text-3xl font-semibold text-black italic w-full px-8 py-4 text-right`}>
            {movie.tagline}
        </q>
    } else {
        return <span className = 'block text-3xl font-semibold py-4'>&nbsp;</span>
    }
}
function CrewMember({ movie, job, label }) {
    const people = movie?.crew?.filter(p => p.job === job)

    if(people?.length !== 0)
        return <div>
            <dt className = 'font-bold text-sm'>{ label }</dt>
            { people?.map(p => <dd className = 'text-sm' key = { `${ job }/${ p.id }` }>{ p.name }</dd>) }
        </div>
    else return null
}
function Links({ movie }) {
    const resources = movie?.resources?.filter(r => !['POSTER', 'BACKDROP', 'TRAILER'].includes(r.type))
    let links

    if(resources?.length === 0) {
        links = <span className = 'block p-8 text-center bg-gray-300 font-bold'>
            No se han encontrado enlaces!
        </span>
    } else {
        links = <ul className = 'space-y-4'>
            {
                resources?.map(r => <PlatformLink key = { r.type } type = { r.type } url = { r.url } />)
            }
        </ul>
    }


    return <>
        <h2 className = 'font-bold text-2xl'>Ver ahora</h2>
        <Separator />
        { links }
    </>
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
function PlatformLink({ type = '', url = '', ...props }) {
    switch (type) {
        case 'DISNEY_PLUS':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { Disney } alt = 'Disney+ logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en
                </span>
            </a>
        case 'GOOGLE_PLAY':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { Play } alt = 'Google Play logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en Google Play
                </span>
            </a>
        case 'HBO':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { HBO } alt = 'HBO logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en HBO
                </span>
            </a>
        case 'ITUNES':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { ITunes } alt = 'iTunes logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en iTunes
                </span>
            </a>
        case 'NETFLIX':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { Netflix } alt = 'Netflix logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en Netflix
                </span>
            </a>
        case 'PRIME_VIDEO':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { Prime } alt = 'Prime Video logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en Prime Video
                </span>
            </a>
        case 'YOUTUBE':
            return <a target = '_blank'
                      rel = 'noreferrer'
                      href = { url }
                      className = {`flex items-center space-x-2 overflow-hidden h-16 w-full bg-white
                                    transform transition duration-200 
                                    hover:translate-x-8 hover:scale-105`}>
                <img src = { Youtube } alt = 'YouTube logo'
                     className = 'rounded-lg w-16 h-16'
                />
                <span className = 'font-bold'>
                    Reproducir en YouTube
                </span>
            </a>
        default: return null
    }
}
function Trailer({ movie, ...props }) {
    const trailer = movie?.resources?.find(r => r.type === 'TRAILER')

    if(trailer)
        return <ReactPlayer url = { trailer.url } { ...props } />
    else
        return <span className = 'text-white text-xl font-semibold p-8 backdrop-filter backdrop-blur bg-red-500 bg-opacity-30'>No se han encontrado trailers!</span>
}